package com.alex.blog.repository.impl;

import com.alex.blog.model.Comment;
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
public class JdbcNativeCommentRepositoryImpl implements CommentRepository {

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
    public void delete(Long id) {
        String sqlDelete = """
                    DELETE FROM comments WHERE id = ?
                """;
        jdbcTemplate.update(sqlDelete, id);
    }

    @Override
    public Comment update(Comment comment) {
        String sqlUpdate = """
                UPDATE comments SET text=?, post_id=?
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
    public void deleteByPostId(Long postId) {
        String sqlDelete = """
                DELETE FROM comments WHERE post_id = ?
                """;
        jdbcTemplate.update(sqlDelete, postId);
    }

    @Override
    public List<Comment> findCommentsByPostId(Long postId) {
        String sqlSelect = """
                SELECT id,text,post_id FROM comments WHERE post_id = ?
                """;
        return jdbcTemplate.query(sqlSelect, getRowMapper(), postId);

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

    private Comment map(KeyHolder keyHolder) {
        return keyHolder.getKeyList().stream().map(m -> {
            Comment comment = new Comment();
            comment.setId((Long) m.get(Comment.Fields.id));
            comment.setText((String) m.get(Comment.Fields.text));
            comment.setPostId((Long) m.get("post_id"));
            return comment;
        }).findFirst().orElseThrow();
    }
}
