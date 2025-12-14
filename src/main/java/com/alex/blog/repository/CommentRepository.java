package com.alex.blog.repository;

import com.alex.blog.model.Comment;
import com.alex.blog.model.Post;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    Optional<Comment> findById(Long id);
    Comment save(Comment comment);
    void delete(Long id);
    Comment update(Comment comment);
    void deleteByPostId(Long postId);
    List<Comment> findCommentsByPostId(Long postId);
    
}
