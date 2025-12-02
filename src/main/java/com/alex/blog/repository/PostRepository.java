package com.alex.blog.repository;

import com.alex.blog.model.Post;
import java.util.Optional;

public interface PostRepository {
    Optional<Post> findById(Long id);

    Post save(Post post);
    void delete(Long id);
    Post update(Post post);
    Long incrementLikesCount(Long postId);
    void updateImagePath(Long postId, String imagePath);
    Long incrementCommentsCount(Long postId);
}
