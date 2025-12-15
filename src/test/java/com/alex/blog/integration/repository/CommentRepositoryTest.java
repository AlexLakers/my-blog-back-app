package com.alex.blog.integration.repository;

import com.alex.blog.model.Comment;
import com.alex.blog.integration.BaseIntegrationTest;
import com.alex.blog.repository.CommentRepository;
import com.alex.blog.repository.PostSearchRepository;
import com.alex.blog.repository.impl.JdbcNativeCommentRepositoryImpl;
import com.alex.blog.repository.impl.JdbcNativePostSearchImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

@JdbcTest
@Sql("classpath:data-test.sql")
class CommentRepositoryTest extends BaseIntegrationTest {

    private static final Comment comment = new Comment(1L, "test comment1", 1L);
    private final static Long VALID_ID = 1L;
    private final static Long INVALID_ID = 1000000L;

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findById_shouldReturnOptionalWithComment() {
        Optional<Comment> maybeComment = commentRepository.findById(VALID_ID);

        Assertions.assertThat(maybeComment.isPresent()).isTrue();
        Assertions.assertThat(maybeComment.get()).isEqualTo(comment);
    }

    @Test
    void save_shouldSaveCommentAndReturnPersistComment() {
        Comment givenComment = new Comment(null, "test comment100", 1L);

        Comment savedComment = commentRepository.save(givenComment);

        Assertions.assertThat(savedComment).hasFieldOrPropertyWithValue("id", 7L);
    }

    @Test
    void delete_shouldDeleteComment() {
        commentRepository.delete(VALID_ID);

        Boolean expectedExists = jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM comments WHERE id = ?)", Boolean.class, VALID_ID);

        Assertions.assertThat(expectedExists).isFalse();
    }


    @Test
    void update_shouldReturnUpdatedComment() {
        Comment givenComment = new Comment(VALID_ID, "Updated comment1", VALID_ID);

        Comment updatedComment = commentRepository.update(givenComment);

        Assertions.assertThat(updatedComment).isEqualTo(givenComment);
    }

    @Test
    void deleteByPostId() {
        commentRepository.deleteByPostId(VALID_ID);

        Boolean expectedExists = jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM comments WHERE id = ?)", Boolean.class, VALID_ID);

        Assertions.assertThat(expectedExists).isFalse();
    }

    @Test
    void findCommentsByPostId_shouldReturnComments() {
        List<Comment> comments = commentRepository.findCommentsByPostId(VALID_ID);

        Assertions.assertThat(comments).isNotNull().hasSize(2).contains(comment);
    }

    @TestConfiguration
    static class TestCommentRepositoryConfig {
        @Bean
        public CommentRepository commentRepository(JdbcTemplate jdbcTemplate) {
            return new JdbcNativeCommentRepositoryImpl(jdbcTemplate);
        }
    }

}