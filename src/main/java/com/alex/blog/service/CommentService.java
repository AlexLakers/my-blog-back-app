package com.alex.blog.service;

import com.alex.blog.api.dto.CommentReadDto;
import com.alex.blog.api.dto.CommentCreateDto;
import com.alex.blog.api.dto.CommentUpdateDto;

import java.util.List;

public interface CommentService {
    CommentReadDto findOneComment(Long postId, Long commentId);
    CommentReadDto saveComment(Long postId,CommentCreateDto commentCreateDto);
    CommentReadDto updateComment(Long postId,Long commentId,CommentUpdateDto commentUpdateDto);
    void deleteComment(Long postId,Long commentId);
    List<CommentReadDto> findCommentsByPostId(Long postId);
}
