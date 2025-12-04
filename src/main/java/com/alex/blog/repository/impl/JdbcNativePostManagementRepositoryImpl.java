package com.alex.blog.repository.impl;

import com.alex.blog.model.Post;
import com.alex.blog.repository.PostManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class JdbcNativePostManagementRepositoryImpl implements PostManagementRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @Override
    public boolean existsById(Long postId) {

        String sqlExists = """ 
                SELECT EXISTS (SELECT 1 FROM comments WHERE id = ?)
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sqlExists, Boolean.class, postId));
    }

    @Override
    public String getImagePath(Long postId) {
        String sqlSelect = """
                SELECT image_path FROM posts WHERE id = ?
                """;
        return jdbcTemplate.queryForObject(sqlSelect, String.class, postId);

    }

    @Override
    public void delete(Long id) {
        String sqlDel = """
                DELETE FROM posts WHERE id = ?
                """;

        jdbcTemplate.update(sqlDel, id);
    }

    @Override
    public Long incrementLikesCount(Long postId) {
        String sqlUpdate = """
                UPDATE posts SET likes_count = likes_count + 1
                WHERE id = ?
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(sqlUpdate, new String[]{"likes_count"});
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

        Long updatedId = updatePostWithoutTags(post);

        deleteLinkPostsTags(updatedId);

        saveLinkPostsTags(updatedId, post.getTags());

        post.setId(updatedId);
        return post;

    }

    private Long updatePostWithoutTags(Post post) {
        String sqlUpdatePost = """
                UPDATE posts SET title=?, text=?
                WHERE id=?
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(sqlUpdatePost, new String[]{"id"});
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            ps.setLong(3, post.getId());
            return ps;
        }, keyHolder);

        return (Long) keyHolder.getKeyList().getFirst().get("id");
    }

    private void deleteLinkPostsTags(Long postId) {
        String sqlDelete = """
                DELETE FROM posts_tags
                WHERE post_id=?
                """;
        jdbcTemplate.update(sqlDelete, postId);
    }

    private void saveLinkPostsTags(Long postId, List<String> tags) {
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

    private void saveTags(List<String> tags) {
        SqlParameterSource[] batchArgsTags = tags.stream()
                .map(tagName -> new MapSqlParameterSource("tagName", tagName))
                .toArray(SqlParameterSource[]::new);

        String sqlInsertTags = """
                INSERT INTO tags (name)
                SELECT :tagName
                WHERE NOT EXISTS (SELECT 1 FROM tags WHERE name = :tagName)
                """;

        namedParameterJdbcTemplate.batchUpdate(sqlInsertTags, batchArgsTags);
    }

    private Long savePostWithoutTags(Post post) {
        String sqlInsertPost = """
                INSERT INTO posts (title, text)
                 VALUES (?,?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(sqlInsertPost, new String[]{"id"});
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            return ps;
        }, keyHolder);

        return (Long) keyHolder.getKeyList().getFirst().get("id");
    }

    @Override
    @Transactional
    public Post save(Post post) {
        Long savedPostId = savePostWithoutTags(post);

        saveTags(post.getTags());

        saveLinkPostsTags(savedPostId, post.getTags());

        post.setId(savedPostId);
        return post;
    }

}
