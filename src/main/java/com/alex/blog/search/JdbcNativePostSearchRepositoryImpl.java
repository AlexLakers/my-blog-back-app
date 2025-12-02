package com.alex.blog.search;

import com.alex.blog.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class JdbcNativePostSearchRepositoryImpl implements PostSearchRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @Override
    public Page<Post> findByTitleContainingAndTag(String title, String tag, Pageable pageable) {
        return null;
    }

    public Page<Post> findByTitleContainingAndTag(Criteria criteria, Pageable pageable) {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource("tags", criteria.tags());
        sqlParameterSource.addValue("title", criteria.title());

        String sqlCount = new QueryBuilder("SELECT COUNT (DISTINCT p.id) FROM posts p")
                .addJoinIf(criteria.hasTags(),
                        "JOIN posts_tags pt ON p.id = pt.post_id JOIN tags t ON pt.tag_id = t.id")
                .addConditionIf(criteria.hasTags(), "t.name IN (:tags)",
                        () -> sqlParameterSource.addValue("tags", criteria.tags()))
                .addConditionIf(criteria.hasTitle(), "p.title LIKE :title",
                        () -> sqlParameterSource.addValue("title", "%" + criteria.title() + "%"))
                .build();


        Long countElements = namedParameterJdbcTemplate.queryForObject(sqlCount, sqlParameterSource, Long.class);
        Integer offset = criteria.pageNumber() * criteria.pageSize();

        sqlParameterSource.addValue("limit", criteria.pageSize());
        sqlParameterSource.addValue("offset", offset);
        sqlParameterSource.addValue("title", criteria.title());

        QueryBuilder queryBuilder = new QueryBuilder("""
                SELECT p.id,p.title p.text  FROM posts p
                """);

        String sqlSelect = queryBuilder
                .addJoinIf(criteria.hasTags(),
                        "JOIN posts_tags pt ON p.id = pt.post_id JOIN tags t ON pt.tag_id = t.id")
                .addConditionIf(criteria.hasTags(), "t.name IN (:tags)",
                        () -> sqlParameterSource.addValue("tags", criteria.tags()))
                .addConditionIf(criteria.hasTitle(), "p.title LIKE :title",
                        () -> sqlParameterSource.addValue("title", "%" + criteria.title() + "%"))
                .append(" ORDER BY p.created_at DESC LIMIT :limit OFFSET :offset")
                .build();
        List<Post> posts = namedParameterJdbcTemplate.query(sqlSelect, sqlParameterSource, getRowMapperForPost());

        List<Long> postsIds = posts.stream()
                .map(Post::getId).toList();

        Map<Long, List<String>> tagsForPosts = getTagsForPostsByPostIds(postsIds);

        posts.forEach(post -> {
                    post.setTags(tagsForPosts.getOrDefault(post.getId(), List.of()));
                }

        );

        return new PageImpl<>(posts, PageRequest.of(criteria.pageNumber(), criteria.pageSize()), countElements);
    }


    private Map<Long, List<String>> getTagsForPostsByPostIds(List<Long> postsIds) {
        SqlParameterSource params = new MapSqlParameterSource("postsIds", postsIds);
        Map<Long, List<String>> tags = new HashMap<>();
        String sqlSelect = """
                SELECT pt.post_id, pt.name FROM post_tags AS pt
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


    private RowMapper<Post> getRowMapperForPost() {
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

