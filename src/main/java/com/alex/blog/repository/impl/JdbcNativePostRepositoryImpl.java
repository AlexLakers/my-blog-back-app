package com.alex.blog.repository.impl;

import com.alex.blog.model.Post;
import com.alex.blog.repository.PostRepository;
import com.alex.blog.search.Criteria;
import com.alex.blog.search.QueryBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class JdbcNativePostRepositoryImpl implements PostRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public Optional<Post> findById(Long id) {
        String sql = """
                SELECT p.id, p.title, p.text, p.image_path, p.likes_count, p.comments_count
                FROM posts AS p
                WHERE p.id = ?
                """;
        try {
            Optional<Post> maybePost = Optional.ofNullable(jdbcTemplate.queryForObject(sql, getRowMapperPost(), id));

            maybePost.ifPresent(post -> {
                post.setTags(findTagsByPostId(post.getId()));
            });
            return maybePost;

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    private List<String> findTagsByPostId(Long postId) {
        String sql = """
                SELECT t.name FROM tags AS t
                JOIN posts_tags AS pt ON pt.tag_id = t.id
                WHERE pt.post_id = ?
                """;
        return jdbcTemplate.query(sql, (rs, rc) -> rs.getString("name"), postId);
    }

    private RowMapper<Post> getRowMapperPost() {
        return (rs, rc) -> {
            Post post = new Post();
            post.setId(rs.getLong(Post.Fields.id));
            post.setTitle(rs.getString(Post.Fields.title));
            post.setText(rs.getString(Post.Fields.text));
            post.setLikesCount(rs.getLong("likes_count"));
            post.setCommentsCount(rs.getLong("comments_count"));
            return post;

        };
    }


    //On service layer drop comments too(manual before cascade)
    @Override
    public void delete(Long id) {
        String sqlDelPosts = """
                DELETE FROM posts WHERE id = ?
                """;

        jdbcTemplate.update(sqlDelPosts, id);
    }


    @Override
    public Long incrementLikesCount(Long postId) {
        String sql = """
                UPDATE posts SET likes_count = likes_count + 1
                WHERE id = ?
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(sql, new String[]{"likes_count"});
            ps.setLong(1, postId);
            return ps;
        }, keyHolder);

        return (Long) keyHolder.getKeyList().getFirst().get("likes_count");
    }

    @Override
    public void updateImagePath(Long postId, String imagePath) {
        String sqlUpdate = """
                UPDATE posts SET image_path = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(sqlUpdate, imagePath, postId);
    }


    @Override
    public Long incrementCommentsCount(Long postId) {
        String sql = """
                    UPDATE posts
                    SET  comments_count=comments_count+1
                    WHERE id=?
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(sql, new String[]{"comments_count"});
            ps.setLong(1, postId);
            return ps;
        }, keyHolder);

        return (Long) keyHolder.getKeyList().getFirst().get("comments_count");
    }


    @Override
    public Post update(Post post) {
        insertTagsAndLinkUsingBatch(post.getId(), post.getTags());

        String sqlUpdate = """
                UPDATE posts SET title=?, text=?
                WHERE id=?
                """;

       // jdbcTemplate.update(sqlUpdate,post.getId());
        return execModifySqlWithFullReturn(post,sqlUpdate);
    }



    private void insertTagsAndLinkUsingBatch(Long postId, List<String> tags) {
        String sqlDelete = """
                DELETE FROM posts_tags
                WHERE post_id=:postId
                """;

        jdbcTemplate.update(sqlDelete,postId);

        SqlParameterSource[] batchArgsTags = tags.stream()
                .map(tagName -> new MapSqlParameterSource("tagName", tagName))
                .toArray(SqlParameterSource[]::new);

        String sqlInsertTags = """
                INSERT INTO tags (name)
                SELECT :tagName
                WHERE NOT EXISTS (SELECT 1 FROM tags WHERE name = :tagName)
                """;

        namedParameterJdbcTemplate.batchUpdate(sqlInsertTags, batchArgsTags);


        SqlParameterSource[] batchArgsPostTag = tags.stream()
                .map(tagName -> new MapSqlParameterSource()
                        .addValue("postId", postId)
                        .addValue("tagName", tagName))
                .toArray(SqlParameterSource[]::new);

        String sqlLinkTagsToPost = """
                INSERT INTO posts_tags (post_id, tag_id)
                SELECT  :postId, id FROM tags
                WHERE name = :tagName
                """;


        namedParameterJdbcTemplate.batchUpdate(sqlLinkTagsToPost, batchArgsPostTag);
    }

    @Override
    public Post save(Post post) {
        insertTagsAndLinkUsingBatch(post.getId(), post.getTags());

        String sqlInsertPost = """
                INSERT INTO posts (title, text)
                 VALUES(?,?)
                """;

        return execModifySqlWithFullReturn(post,sqlInsertPost);

    }
    private Post execModifySqlWithFullReturn(Post post, String sqlModify) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sqlModify, new String[]{"id", "title", "text", "comments_count", "likes_count"});
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            if (post.getId() != null) {
                ps.setLong(3, post.getId());
            }
            return ps;
        }, keyHolder);

        return map(keyHolder);
    }


    private Post map(KeyHolder keyHolder) {
        return keyHolder.getKeyList().stream().map(m -> {
            Post savedPost = new Post();
            savedPost.setId((Long) m.get(Post.Fields.id));
            savedPost.setTitle((String) m.get(Post.Fields.title));
            savedPost.setText((String) m.get(Post.Fields.text));
            savedPost.setLikesCount((Long) m.get("likes_count"));
            savedPost.setCommentsCount((Long) m.get("comments_count"));
            return savedPost;
        }).findFirst().orElseThrow();
    }



}
