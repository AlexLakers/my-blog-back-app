package com.alex.blog.repository.impl;

import com.alex.blog.model.Post;
import com.alex.blog.repository.PostSearchRepository;
import com.alex.blog.search.Criteria;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcNativePostSearchImpl implements PostSearchRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;


    public Page<Post> findPostsByCriteriaAndPageable(Criteria criteria, Pageable pageable) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sqlWhere = buildWhere(criteria, params, pageable);

        StringBuilder sqlCount = new StringBuilder("""
                SELECT COUNT (DISTINCT p.id) FROM posts p
                JOIN posts_tags pt ON p.id = pt.post_id
                JOIN tags t ON pt.tag_id = t.id
                """)
                .append(sqlWhere);

        Long countElements = namedParameterJdbcTemplate.queryForObject(sqlCount.toString(), params, Long.class);

        StringBuilder sqlSelect = new StringBuilder("""
                SELECT p.id,p.title,p.text,p.likes_count,p.comments_count FROM posts AS p
                JOIN posts_tags AS pt ON p.id = pt.post_id
                JOIN tags t ON pt.tag_id = t.id
                """)
                .append(sqlWhere)
                .append("ORDER BY p.id LIMIT :limit OFFSET :offset");

        List<Post> postsWithoutTags = namedParameterJdbcTemplate.query(sqlSelect.toString(), params, getRowMapperPost());
        List<Post> posts = fetchTags(postsWithoutTags);

        return new PageImpl<>(posts, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), countElements);
    }

    private List<Post> fetchTags(List<Post> posts) {
        List<Long> postsIds = posts.stream()
                .map(Post::getId).toList();

        Map<Long, List<String>> tagsForPosts = getTagsForPostsByPostIds(postsIds);

        posts.forEach(post -> {
                    post.setTags(tagsForPosts.getOrDefault(post.getId(), List.of()));
                }
        );
        return posts;
    }

    private String buildWhere(Criteria criteria, MapSqlParameterSource params, Pageable pageable) {

        List<String> conditions = new ArrayList<>();
        if (criteria.title() != null && !criteria.title().isEmpty()) {
            params.addValue("title", "%" +criteria.title()+ "%");
            conditions.add(" p.title LIKE :title ");
        }
        if (criteria.tags() != null && !criteria.tags().isEmpty()) {
            params.addValue("tags", criteria.tags());
            conditions.add(" t.name IN(:tags) ");
        }
        params.addValue("limit", pageable.getPageSize());
        params.addValue("offset", pageable.getPageNumber() * pageable.getOffset());

        return conditions.stream().collect(Collectors.joining(" AND ", " WHERE ", ""));
    }


    private Map<Long, List<String>> getTagsForPostsByPostIds(List<Long> postsIds) {
        SqlParameterSource params = new MapSqlParameterSource("postsIds", postsIds);
        Map<Long, List<String>> tags = new HashMap<>();
        String sqlSelect = """
                SELECT pt.post_id, t.name
                FROM posts_tags AS pt
                JOIN tags AS t ON pt.tag_id = t.id
                WHERE pt.post_id IN (:postsIds)
                """;
        namedParameterJdbcTemplate.query(sqlSelect, params,
                (rs, rc) -> {
                    Long postId = rs.getLong("post_id");
                    String tag = rs.getString("name");
                    tags.computeIfAbsent(postId, k -> new ArrayList<>()).add(tag);
                    return tags;
                });
        return tags;
    }

    @Override
    public Optional<Post> findPostById(Long id) {
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

}
