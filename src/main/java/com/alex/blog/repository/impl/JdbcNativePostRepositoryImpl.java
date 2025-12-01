package com.alex.blog.repository.impl;

import com.alex.blog.model.Post;
import com.alex.blog.repository.PostRepository;
import com.alex.blog.repository.Criteria;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
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
    public Post save(Post post) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public boolean update(Post post) {
        return false;
    }

    @Override
    public Page<Post> findAll(Criteria criteria) {
        return null;
    }

    @Override
    public Long incrementLikesCount(Long postId) {
        return 0L;
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




}
