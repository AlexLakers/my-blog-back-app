package com.alex.blog.repository.impl;

import com.alex.blog.model.Post;
import com.alex.blog.repository.PostRepository;
import com.alex.blog.repository.Criteria;
import com.alex.blog.repository.QueryBuilder;
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

    @Override
    public Optional<Post> findById(Long id) {
        String sql = """
                SELECT p.id, p.title, p.text, p.image_path, p.likes_count, p.comments_count,
                       STRING_AGG(t.name, ',') AS tags
                FROM posts AS p
                LEFT JOIN posts_tags AS pt ON p.id = pt.post_id
                LEFT JOIN tags AS t ON pt.tag_id = t.id
                WHERE p.id = ?
                GROUP BY p.id, p.title, p.text, p.image_path, p.likes_count, p.comments_count-- все поля Posts
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, getRowMapperForPostWithTags(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }


    @Override
    public boolean delete(Long id) {
        String sqlDelPosts = """
                DELETE FROM posts WHERE id = ?
                """;

        return jdbcTemplate.update(sqlDelPosts, id) > 0;
    }


    @Override
    public Page<Post> findAll(Criteria criteria) {
        return null;
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
    public boolean updateImagePath(Long postId, String imagePath) {
        String sqlUpdate = """
                UPDATE posts SET image_path = ?
                WHERE id = ?
                """;
        return jdbcTemplate.update(sqlUpdate, postId, imagePath) > 0;
    }

    private RowMapper<Post> getRowMapperForPostWithoutTags() {
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

    private RowMapper<Post> getRowMapperForPostWithTags() {
        return (rs, rc) -> {
            Post post = new Post();
            post.setId(rs.getLong(Post.Fields.id));
            post.setTitle(rs.getString(Post.Fields.title));
            post.setText(rs.getString(Post.Fields.text));
            post.setLikesCount(rs.getLong("likes_count"));
            post.setCommentsCount(rs.getLong("comments_count"));
            post.setTags(getTags(rs));
            return post;

        };
    }

    private List<String> getTags(ResultSet rs) throws SQLException {
        return Optional.ofNullable(rs.getString(Post.Fields.tags))
                .map(ts -> ts.split(","))
                .map(Arrays::asList)
                .orElseGet(Collections::emptyList);

    }
    @Override
    public boolean update(Post post) {
        String sqlUpdate = """
                UPDATE posts SET title=?, text=?
                WHERE id=?
                """;

        Post updatedPost = execModifySqlWithFullReturn(post, sqlUpdate);


        String sqlDelTags = """
                DELETE FROM tags AS t
                USING posts_tags AS pt
                WHERE pt.tag_id=t.id
                AND pt.post_id=?
                """;

        jdbcTemplate.update(sqlDelTags, post.getId());

        insertTagsAndLink(updatedPost.getId(),post.getTags());

        updatedPost.setTags(post.getTags());

        return true;
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

    private void insertTagsAndLink(Long postId, List<String> tags) {

        SqlParameterSource[] batchArgs = tags.stream()
                .map(tagName -> new MapSqlParameterSource("tagName", tagName))
                .toArray(SqlParameterSource[]::new);

        String sqlInsertTags = """
                INSERT INTO tags (name)
                SELECT :tagName
                WHERE NOT EXISTS (SELECT 1 FROM tags WHERE name = :tagName)
                """;

        namedParameterJdbcTemplate.batchUpdate(sqlInsertTags, batchArgs);


        SqlParameterSource[] batchArgs1 = tags.stream()
                .map(tagName -> new MapSqlParameterSource()
                        .addValue("postId", postId)
                        .addValue("tagName", tagName))
                .toArray(SqlParameterSource[]::new);

        String sqlLinkTagsToPost = """
                INSERT INTO posts_tags (post_id, tag_id)
                SELECT  :postId, id FROM tags
                WHERE name = :tagName
                """;


        namedParameterJdbcTemplate.batchUpdate(sqlLinkTagsToPost, batchArgs1);
    }

    @Override
    public Post save(Post post) {
        String sqlInsertPost = """
                INSERT INTO posts (title, text)
                 VALUES(?,?)
                """;
        Post savedPost = execModifySqlWithFullReturn(post, sqlInsertPost);

        insertTagsAndLink(savedPost.getId(), post.getTags());

        savedPost.setTags(post.getTags());
        return savedPost;

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
