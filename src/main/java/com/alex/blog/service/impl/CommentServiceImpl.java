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
import com.alex.blog.service.CommentService;
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
    public CommentReadDto findOneComment(Long postId, Long commentId) {

        return commentRepository.findById(commentId)
                .map(commentMapper::toCommentReadDto)
                .orElseThrow(() -> new EntityNotFoundException("The comment with id: %d not found"
                        .formatted(commentId)));
    }

    @Override
    public CommentReadDto saveComment(Long postId, CommentCreateDto commentCreateDto) {

        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException("The post with id: %d not found"
                    .formatted(commentCreateDto.postId()));
        }
       CommentReadDto commentReadDto=Optional.ofNullable(commentCreateDto)
                .map(commentMapper::toComment)
                .map(commentRepository::save)
                .map(commentMapper::toCommentReadDto)
               .orElseThrow(()-> new EntityCreationException("An error has been detected during creating"));

        postManagementRepository.incrementCommentsCount(commentReadDto.postId(),1L);

        return commentReadDto;
    }

    @Override
    public CommentReadDto updateComment(Long postId, Long commentId, CommentUpdateDto commentUpdateDto) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException("The post with id: %d not exists".formatted(postId));
        }
        Optional<Comment> maybeComment = Optional.ofNullable(commentId)
                .flatMap(commentRepository::findById)
                .map(comm -> {
                    commentMapper.updateComment(commentUpdateDto, comm);
                    return comm;
                });


        if (maybeComment.isEmpty()) {
            throw new EntityNotFoundException("The comment with id: %d not found".formatted(commentId));
        }

        Comment updatedComment = commentRepository.update(maybeComment.get());

        return commentMapper.toCommentReadDto(updatedComment);

    }

    @Override
    public void deleteComment(Long postId, Long commentId) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException("The post with id: %d not exists".formatted(postId));
        }
        Optional<Comment> maybeComment = commentRepository.findById(commentId);

        if (maybeComment.isEmpty()) {
            throw new EntityNotFoundException("The comment with id: %d not found".formatted(commentId));
        }
        commentRepository.delete(commentId);
        System.out.println(postManagementRepository.incrementCommentsCount(postId,-1L));

    }

    @Override
    public List<CommentReadDto> findCommentsByPostId(Long postId) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException("The post not found by id:%d".formatted(postId));
        }
        return commentRepository.findCommentsByPostId(postId).stream()
                .map(commentMapper::toCommentReadDto).toList();
    }

}
