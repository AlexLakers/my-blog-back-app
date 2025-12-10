package com.alex.blog.repository;

import com.alex.blog.model.Post;

import java.util.Optional;

public interface PostManagementRepository {
    Post save(Post post);
    void delete(Long id);
    Post update(Post post);
    Long incrementLikesCount(Long postId);
    void updateImagePath(Long postId, String imagePath);
    Long incrementCommentsCount(Long postId,Long incValue);
    boolean existsById(Long id);
    boolean existsByTitle(String title);
    Optional<String> getImagePath(Long postId);

}
