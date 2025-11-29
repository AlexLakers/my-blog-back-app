package com.alex.blog.repository;

import com.alex.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostRepository {
    Optional<Post> findById(Long id);
    Post save(Post post);
    boolean delete(Long id);
    boolean update(Post post);
    Page<Post> findAll(Criteria criteria);
    Long incrementLikesCount();
}
