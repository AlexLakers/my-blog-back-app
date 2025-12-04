package com.alex.blog.repository;

import com.alex.blog.model.Post;
import com.alex.blog.search.Criteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostSearchRepository {
    Page<Post> findPostsByCriteriaAndPageable(Criteria criteria, Pageable pageable);
    Optional<Post> findPostById(Long postId);
}
