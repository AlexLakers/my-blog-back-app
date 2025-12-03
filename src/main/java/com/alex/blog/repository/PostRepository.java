package com.alex.blog.repository;

import com.alex.blog.model.Post;
import com.alex.blog.search.Criteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostRepository {
    Optional<Post> findById(Long id);
    Post save(Post post);
    void delete(Long id);
    Post update(Post post);
    Long incrementLikesCount(Long postId);
    void updateImagePath(Long postId, String imagePath);
    Long incrementCommentsCount(Long postId);
    Page<Post> findPostsByCriteriaAndPageable(Criteria criteria, Pageable pageable);
    boolean existsById(Long id);
}
