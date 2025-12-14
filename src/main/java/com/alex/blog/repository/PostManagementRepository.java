package com.alex.blog.repository;

import com.alex.blog.model.Post;

import java.util.Optional;

public interface PostManagementRepository {
    Post save(Post post);
    void delete(Long id);
    Post update(Post post);
    Long incrementLikesCount(Long postId);
    boolean updateImage(Long postId, byte[] image);
    Long incrementCommentsCount(Long postId,Long incValue);
    boolean existsById(Long id);
    boolean existsByTitle(String title);
    Optional<byte[]> getImage(Long postId);

}
