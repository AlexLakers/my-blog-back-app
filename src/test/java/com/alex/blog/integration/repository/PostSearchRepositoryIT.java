package com.alex.blog.integration.repository;

import com.alex.blog.model.Post;
import com.alex.blog.repository.PostSearchRepository;
import com.alex.blog.repository.impl.JdbcNativePostSearchImpl;
import com.alex.blog.search.Criteria;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@JdbcTest
@Sql("classpath:data-test.sql")
@ActiveProfiles("test")
class PostSearchRepositoryIT {

    private final static Post post = new Post(1L, "test title1", "test desc1", List.of("test_tag1"), null, 2L, 3L);

    private final static Long VALID_ID=1L;
    private final static Long INVALID_ID=1000000L;
    @Autowired
    private JdbcNativePostSearchImpl jdbcNativePostSearchImpl;


    @Test
    public void findOnePostById_returnNotEmptyOptional(){
        Optional<Post> actualPost=jdbcNativePostSearchImpl.findPostById(VALID_ID);

        Assertions.assertThat(actualPost.isPresent()).isTrue();
        Assertions.assertThat(actualPost.get().getId()).isEqualTo(VALID_ID);
    }

    @Test
    public void findOnePostById_ReturnEmptyOptional(){
        Optional<Post> actualPost=jdbcNativePostSearchImpl.findPostById(INVALID_ID);

        Assertions.assertThat(actualPost.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getArgsForSearchByCriteria")
    void findPostsByCriteriaAndPageable_shouldReturnPage(Criteria givenCriteria, Pageable givenPageable, Integer expSize) {
        Page<Post> actualpage = jdbcNativePostSearchImpl.findPostsByCriteriaAndPageable(givenCriteria, givenPageable);


        Assertions.assertThat(actualpage.getContent())
                .isNotNull()
                .hasSize(expSize)
                .contains(post);
    }


    public static Stream<Arguments> getArgsForSearchByCriteria() {
        Pageable pageableOneSize = PageRequest.of(0, 1);
        Pageable pageableTwoSize = PageRequest.of(0, 2);

        Criteria criteriaForAll = new Criteria("test title", List.of("test_tag1"));
        Criteria criteriaForTitle = new Criteria("test title", null);
        Criteria criteriaForTags = new Criteria(null, List.of("test_tag1"));

        return Stream.of(
                Arguments.of(criteriaForAll, pageableOneSize, 1),
                Arguments.of(criteriaForAll, pageableTwoSize, 2),
                Arguments.of(criteriaForTitle, pageableOneSize, 1),
                Arguments.of(criteriaForTags, pageableTwoSize, 2)

        );
    }
    @TestConfiguration
    static class TestPostSearchRepositoryConfig {
        @Bean
        public PostSearchRepository postSearchRepository(
                NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                JdbcTemplate jdbcTemplate) {

            return new JdbcNativePostSearchImpl(namedParameterJdbcTemplate, jdbcTemplate);
        }
    }

}