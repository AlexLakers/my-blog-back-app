package com.alex.blog.service;

import com.alex.blog.api.dto.CommentCreateDto;
import com.alex.blog.api.dto.CommentReadDto;
import com.alex.blog.api.dto.CommentUpdateDto;
import com.alex.blog.exception.EntityCreationException;
import com.alex.blog.exception.EntityNotFoundException;
import com.alex.blog.mapper.CommentMapper;
import com.alex.blog.model.Comment;
import com.alex.blog.repository.CommentRepository;
import com.alex.blog.repository.PostManagementRepository;
import com.alex.blog.service.impl.CommentServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private PostManagementRepository postManagementRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private final static Long VALID_ID = 1L;
    private final static Long INVALID_ID = 10000L;
    private final static Comment comment = new Comment(VALID_ID, "test comment", VALID_ID);


    @Test
    void findOneComment_returnOptionalCommentNotEmptySucess() {
        CommentReadDto expectedDto = new CommentReadDto(VALID_ID, "test comment", VALID_ID);
        Mockito.when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        Mockito.when(commentRepository.findById(VALID_ID)).thenReturn(Optional.of(comment));
        Mockito.when(commentMapper.toCommentReadDto(comment)).thenReturn(expectedDto);

        CommentReadDto actual = commentService.findOneComment(VALID_ID, VALID_ID);

        Assertions.assertThat(actual).isEqualTo(expectedDto);

        verify(postManagementRepository, times(1)).existsById(VALID_ID);
        verify(commentRepository, times(1)).findById(VALID_ID);
        verify(commentMapper, times(1)).toCommentReadDto(comment);
    }

    @Test
    void findOneComment_throwForPostEntityNotFoundExceptionFail() {
        Mockito.when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);

        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> commentService.findOneComment(INVALID_ID, VALID_ID))
                .withMessage("The post not found by id:%d".formatted(INVALID_ID));

        verify(commentRepository, times(0)).findById(INVALID_ID);
        verify(commentMapper, times(0)).toCommentReadDto(comment);
    }

    @Test
    void findOneComment_throwForCommentEntityNotFoundExceptionFail() {
        Mockito.when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        Mockito.when(commentRepository.findById(INVALID_ID)).thenReturn(Optional.empty());


        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> commentService.findOneComment(VALID_ID, INVALID_ID))
                .withMessage("The comment not found by id:%d".formatted(INVALID_ID));

        verify(commentMapper, times(0)).toCommentReadDto(comment);
    }

    @Test
    void saveComment_shouldSaveCommentAndReturnPersistComment() {
        CommentCreateDto givenDto = new CommentCreateDto("", VALID_ID);
        CommentReadDto commentReadDto = new CommentReadDto(VALID_ID, "text comment", VALID_ID);
        Mockito.when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        Mockito.when(commentMapper.toComment(Mockito.any(CommentCreateDto.class))).thenReturn(comment);
        Mockito.when(commentRepository.save(Mockito.any(Comment.class))).thenReturn(comment);
        Mockito.when(commentMapper.toCommentReadDto(comment)).thenReturn(commentReadDto);

        CommentReadDto actualDto = commentService.saveComment(VALID_ID, givenDto);

        Assertions.assertThat(actualDto)
                .isEqualTo(commentReadDto)
                .hasFieldOrPropertyWithValue(Comment.Fields.id, VALID_ID);

    }
    @Test
    void saveComment_shouldThrowPostEntityNotFoundExceptionFail() {
        CommentCreateDto givenDto = new CommentCreateDto("", INVALID_ID);
        Mockito.when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);

        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> commentService.saveComment(INVALID_ID, givenDto))
                .withMessage("The post not found by id:%d".formatted(INVALID_ID));

        verify(commentMapper, times(0)).toComment(givenDto);
        verify(commentRepository, times(0)).save(comment);
        verify(commentMapper, times(0)).toCommentReadDto(comment);
    }

    @Test
    void updateComment_shouldReturnUpdatedCommentSuccess() {
        CommentReadDto expectedDto = new CommentReadDto(VALID_ID, "updated comment", VALID_ID);
        CommentUpdateDto givenDto=new CommentUpdateDto(VALID_ID,"updated comment", VALID_ID);

        Mockito.when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        Mockito.when(commentRepository.findById(VALID_ID)).thenReturn(Optional.of(comment));
        Mockito.when(commentRepository.update(comment)).thenReturn(comment);
        Mockito.doNothing().when(commentMapper).updateComment(givenDto,comment);
        Mockito.when(commentMapper.toCommentReadDto(comment)).thenReturn(expectedDto);

        CommentReadDto actualDto=commentService.updateComment(VALID_ID,VALID_ID,givenDto);

        Assertions.assertThat(actualDto).isEqualTo(expectedDto);
        verify(commentRepository, times(1)).update(comment);
    }
    @Test
    void updateComment_shouldPostThrowEntityNotFoundExceptionFail() {
        CommentUpdateDto givenDto=new CommentUpdateDto(VALID_ID,"", VALID_ID);

        Mockito.when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);

        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(()->commentService.updateComment(INVALID_ID,INVALID_ID,givenDto))
                        .withMessage("The post not found by id:%d".formatted(INVALID_ID));

        verify(commentMapper, times(0)).updateComment(givenDto,comment);
        verify(commentRepository, times(0)).update(comment);
        verify(commentMapper, times(0)).toCommentReadDto(comment);
    }


    @Test
    void updateComment_shouldCommentThrowEntityNotFoundExceptionFail() {
        CommentUpdateDto givenDto=new CommentUpdateDto(INVALID_ID,"", VALID_ID);

        Mockito.when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);

        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(()->commentService.updateComment(VALID_ID,INVALID_ID,givenDto))
                .withMessage("The comment not found by id:%d".formatted(INVALID_ID));

        verify(commentMapper, times(0)).updateComment(givenDto,comment);
        verify(commentRepository, times(0)).update(comment);
        verify(commentMapper, times(0)).toCommentReadDto(comment);
    }






    @Test
    void deleteComment_shouldPostThrowEntityNotFoundExceptionFail() {
        Mockito.when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);


        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(()->commentService.deleteComment(INVALID_ID,INVALID_ID))
                .withMessage("The post not found by id:%d".formatted(INVALID_ID));

        verify(commentRepository,times(0)).delete(INVALID_ID);
    }

    @Test
    void findCommentsByPostId_shouldCommentThrowEntityNotFoundExceptionFail() {
        Mockito.when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        Mockito.when(commentRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(()->commentService.deleteComment(VALID_ID,INVALID_ID))
                .withMessage("The comment not found by id:%d".formatted(INVALID_ID));

        verify(commentRepository,times(0)).delete(INVALID_ID);
    }
}