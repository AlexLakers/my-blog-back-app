package com.alex.blog.repository;

import com.alex.blog.model.Comment;
import com.alex.blog.model.Post;
import config.TestDataSourceConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestDataSourceConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class PostManagementRepositoryTest {

    @Autowired
    private PostManagementRepository postManagementRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Comment comment = new Comment(1L, "test comment1", 1L);
    private final static Post post = new Post(1L, "test title1", "test desc1", List.of("test_tag1"), "1/image.jpg", 2L, 3L);
    private final static Long VALID_ID = 1L;

    // @CsvSource({ "alex, 30", "brian, 35", "charles, 40" }) void testWithCsvSource(String name, int age) { assertNotNull(name); assertTrue(age > 0); }.

    @ParameterizedTest
    @CsvSource(
            {"1,true", "1000,false"}
    )
    void existsById_shouldReturnBooleanResult(String idStr, boolean expected) {
        boolean actual = postManagementRepository.existsById(Long.parseLong(idStr));

        Assertions.assertThat(actual).isEqualTo(expected);
    }


    @ParameterizedTest
    @CsvSource(
            {"test title1,true", "unknown text, false"}
    )
    void existsByTitle_shouldReturnBooleanResult(String title, boolean expected) {
        boolean actual = postManagementRepository.existsByTitle(title);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getImagePath_shouldReturnPath() {
        String actualPath = postManagementRepository.getImagePath(VALID_ID);

        Assertions.assertThat(actualPath).isEqualTo(post.getImagePath());
    }


    @Test
    void delete_shouldDeletePost() {
        postManagementRepository.delete(VALID_ID);
        Boolean expectedExists = jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM posts WHERE id = ?)", Boolean.class, VALID_ID);

        Assertions.assertThat(expectedExists).isFalse();
    }

    @Test
    void incrementLikesCount_shouldReturnIncrementLikesCount() {
        // Long expectedLikes = jdbcTemplate.queryForObject("SELECT likes_count FROM posts WHERE id = ?", Long.class, VALID_ID) + 1;
        Long expectedLikes = post.getLikesCount() + 1;

        Long actualLikes = postManagementRepository.incrementLikesCount(VALID_ID);

        Assertions.assertThat(actualLikes).isEqualTo(expectedLikes);
    }

    @Test
    void updateImagePath_shouldUpdatePath() {
        String givenNewPath = "new/path";
        postManagementRepository.updateImagePath(VALID_ID, givenNewPath);

        String expectedPath = jdbcTemplate.queryForObject("SELECT image_path FROM posts WHERE id = ?", String.class, VALID_ID);

        Assertions.assertThat(expectedPath).isEqualTo(givenNewPath);

    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1, 2, -2})
    void incrementCommentsCount_shouldReturnIncrementCommentsCount(int incValue) {
        // Long expectedComments = jdbcTemplate.queryForObject("SELECT comments_count FROM posts WHERE id = ?", Long.class, VALID_ID)+incValue;
        Long expectedComments = post.getCommentsCount() + incValue;

        Long actualComments = postManagementRepository.incrementCommentsCount(VALID_ID, (long) incValue);

        Assertions.assertThat(actualComments).isEqualTo(expectedComments);
    }

    @Test
    void update_shouldReturnUpdatedPost() {
        Post expectedPost = new Post(VALID_ID, "newUpdateTitle", "description", List.of("newUpdateTag"), "new/path", 1L, 1L);

        Post actualPost = postManagementRepository.update(expectedPost);

        Assertions.assertThat(actualPost).isEqualTo(expectedPost);
    }

    @Test
    void save_shouldReturnPersistPost() {
        Post expectedPost = new Post(null, "newTitle", "newDescription", List.of("newCreateTag"), "new/path", 0L, 0L);

        Post savedPost=postManagementRepository.save(expectedPost);

        Assertions.assertThat(savedPost).hasFieldOrPropertyWithValue("id",4L);
    }
}