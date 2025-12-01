package com.alex.blog.repository.impl;

import com.alex.blog.model.Comment;
import com.alex.blog.model.Post;
import com.alex.blog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;


    @Override
    public Comment save(Comment comment) {
        String sqlUpdate = """
                            INSERT INTO comments (text, post_id)
                             VALUES(?,?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sqlUpdate, new String[]{"id", "text", "post_id"});
            ps.setString(1, comment.getText());
            ps.setLong(2, comment.getPostId());
            return ps;
        }, keyHolder);
        return map(keyHolder);
    }

    @Override
    public boolean delete(Long id) {
        String sql= """
                    DELETE FROM comments WHERE id = ?
                """;
        return jdbcTemplate.update(sql, id)>0;
    }

    @Override
    public Comment update(Comment comment) {
        String sqlUpdate = """
                UPDATE posts SET text=?, post_id=?
                WHERE id=?
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sqlUpdate, new String[]{"id", "text", "post_id"});
            ps.setString(1, comment.getText());
            ps.setLong(2, comment.getPostId());
            ps.setLong(3, comment.getId());
            return ps;
        }, keyHolder);

        return map(keyHolder);
    }

    private Comment map(KeyHolder keyHolder) {
        return keyHolder.getKeyList().stream().map(m -> {
            Comment comment = new Comment();
            comment.setId((Long) m.get(Comment.Fields.id));
            comment.setText((String) m.get(Post.Fields.title));
            comment.setPostId((Long) m.get("post_id"));
            return comment;
        }).findFirst().orElseThrow();
    }


    @Override
    public Optional<Comment> findById(Long id) {
        String sql = """
                 SELECT * FROM comments WHERE id = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, getRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Comment> findCommentsByPostId(Long postId) {
        String sql = """
                SELECT * FROM comments WHERE post_id = ?
                """;
        return jdbcTemplate.query(sql, getRowMapper(), postId);
    }


    private RowMapper<Comment> getRowMapper() {
        return (rs, rc) -> {
            Comment comment = new Comment();
            comment.setId(rs.getLong(Comment.Fields.id));
            comment.setText(rs.getString(Comment.Fields.text));
            comment.setPostId(rs.getLong("post_id"));
            return comment;

        };
    }
}
