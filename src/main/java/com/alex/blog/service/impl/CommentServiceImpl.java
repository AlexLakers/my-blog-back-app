package com.alex.blog.service.impl;

import com.alex.blog.api.dto.CommentCreateDto;
import com.alex.blog.api.dto.CommentReadDto;
import com.alex.blog.api.dto.CommentUpdateDto;
import com.alex.blog.exception.EntityNotFoundException;
import com.alex.blog.exception.EntityCreationException;
import com.alex.blog.mapper.CommentMapper;
import com.alex.blog.model.Comment;
import com.alex.blog.repository.CommentRepository;
import com.alex.blog.repository.PostManagementRepository;
import com.alex.blog.repository.PostSearchRepository;
import com.alex.blog.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostManagementRepository postManagementRepository;




    @Override
    public List<CommentReadDto> findCommentsByPostId(Long postId) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException("The post not found by id:%d".formatted(postId));
        }
        return commentRepository.findCommentsByPostId(postId).stream()
                .map(commentMapper::toCommentReadDto).toList();
    }

}
