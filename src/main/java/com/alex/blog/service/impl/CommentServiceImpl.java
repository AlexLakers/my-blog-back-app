package com.alex.blog.service.impl;

import com.alex.blog.aop.annotation.Loggable;
import com.alex.blog.api.dto.CommentCreateDto;
import com.alex.blog.api.dto.CommentReadDto;
import com.alex.blog.api.dto.CommentUpdateDto;
import com.alex.blog.exception.EntityNotFoundException;
import com.alex.blog.exception.EntityCreationException;
import com.alex.blog.mapper.CommentMapper;
import com.alex.blog.model.Comment;
import com.alex.blog.repository.CommentRepository;
import com.alex.blog.repository.PostManagementRepository;
import com.alex.blog.service.CommentService;
import com.alex.blog.service.MessageKey;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostManagementRepository postManagementRepository;
    private final MessageSource messageSource;


    @Loggable
    @Override
    public CommentReadDto findOneComment(Long postId, Long commentId) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{postId}, Locale.ENGLISH));
        }
        return commentRepository.findById(commentId)
                .map(commentMapper::toCommentReadDto)
                .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage(MessageKey.COMMENT_NOT_FOUND, new Object[]{commentId}, Locale.ENGLISH)));
    }

    @Loggable
    @Transactional
    @Override
    public CommentReadDto saveComment(Long postId, CommentCreateDto commentCreateDto) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{commentCreateDto.postId()}, Locale.ENGLISH));

        }

        CommentReadDto commentReadDto = Optional.ofNullable(commentCreateDto)
                .map(commentMapper::toComment)
                .map(commentRepository::save)
                .map(commentMapper::toCommentReadDto)
                .orElseThrow(() -> new EntityCreationException(messageSource.getMessage(MessageKey.COMMENT_CREATION_EX, null, Locale.ENGLISH)));

        postManagementRepository.incrementCommentsCount(commentReadDto.postId(), 1L);

        return commentReadDto;
    }

    @Loggable
    @Transactional
    @Override
    public CommentReadDto updateComment(Long postId, Long commentId, CommentUpdateDto commentUpdateDto) {

        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{postId}, Locale.ENGLISH));
        }
        Optional<Comment> maybeComment = Optional.ofNullable(commentId)
                .flatMap(commentRepository::findById)
                .map(comm -> {
                    commentMapper.updateComment(commentUpdateDto, comm);
                    return comm;
                });

        if (maybeComment.isEmpty()) {
            throw new EntityNotFoundException(messageSource.getMessage(MessageKey.COMMENT_NOT_FOUND, new Object[]{commentId}, Locale.ENGLISH));
        }

        Comment updatedComment = commentRepository.update(maybeComment.get());

        return commentMapper.toCommentReadDto(updatedComment);

    }

    @Loggable
    @Transactional
    @Override
    public void deleteComment(Long postId, Long commentId) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{postId}, Locale.ENGLISH));
        }
        Optional<Comment> maybeComment = commentRepository.findById(commentId);

        if (maybeComment.isEmpty()) {
            throw new EntityNotFoundException(messageSource.getMessage(MessageKey.COMMENT_NOT_FOUND, new Object[]{commentId}, Locale.ENGLISH));
        }
        commentRepository.delete(commentId);
        System.out.println(postManagementRepository.incrementCommentsCount(postId, -1L));

    }

    @Override
    public List<CommentReadDto> findCommentsByPostId(Long postId) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{postId}, Locale.ENGLISH));
        }
        return commentRepository.findCommentsByPostId(postId).stream()
                .map(commentMapper::toCommentReadDto).toList();
    }

}
