package com.alex.blog.service;

import com.alex.blog.api.dto.CommentCreateDto;
import com.alex.blog.api.dto.CommentReadDto;
import com.alex.blog.api.dto.CommentUpdateDto;
import com.alex.blog.config.MessagesConfig;
import com.alex.blog.exception.EntityNotFoundException;
import com.alex.blog.mapper.CommentMapper;
import com.alex.blog.model.Comment;
import com.alex.blog.repository.CommentRepository;
import com.alex.blog.repository.PostManagementRepository;
import com.alex.blog.service.impl.CommentServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CommentServiceTest {

    @MockitoBean
    private CommentRepository commentRepository;
    @MockitoBean
    private CommentMapper commentMapper;
    @MockitoBean
    private PostManagementRepository postManagementRepository;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private CommentService commentService;


    private final static Long VALID_ID = 1L;
    private final static Long INVALID_ID = 10000L;
    private final static Comment comment = new Comment(VALID_ID, "test comment", VALID_ID);


    @Test
    void findOneComment_returnOptionalCommentNotEmptySucess() {
        CommentReadDto expectedDto = new CommentReadDto(VALID_ID, "test comment", VALID_ID);
        when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        when(commentRepository.findById(VALID_ID)).thenReturn(Optional.of(comment));
        when(commentMapper.toCommentReadDto(comment)).thenReturn(expectedDto);

        CommentReadDto actual = commentService.findOneComment(VALID_ID, VALID_ID);

        assertThat(actual).isEqualTo(expectedDto);

        verify(postManagementRepository, times(1)).existsById(VALID_ID);
        verify(commentRepository, times(1)).findById(VALID_ID);
        verify(commentMapper, times(1)).toCommentReadDto(comment);
    }

    @Test
    void findOneComment_throwForPostEntityNotFoundExceptionFail() {
        when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> commentService.findOneComment(INVALID_ID, VALID_ID))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

        verify(commentRepository, times(0)).findById(INVALID_ID);
        verify(commentMapper, times(0)).toCommentReadDto(comment);
    }

    @Test
    void findOneComment_throwForCommentEntityNotFoundExceptionFail() {
        when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        when(commentRepository.findById(INVALID_ID)).thenReturn(Optional.empty());


        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> commentService.findOneComment(VALID_ID, INVALID_ID))
                .withMessage(messageSource.getMessage(MessageKey.COMMENT_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

        verify(commentMapper, times(0)).toCommentReadDto(comment);
    }

    @Test
    void saveComment_shouldSaveCommentAndReturnPersistComment() {
        CommentCreateDto givenDto = new CommentCreateDto("", VALID_ID);
        CommentReadDto commentReadDto = new CommentReadDto(VALID_ID, "text comment", VALID_ID);
        when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        when(commentMapper.toComment(Mockito.any(CommentCreateDto.class))).thenReturn(comment);
        when(commentRepository.save(Mockito.any(Comment.class))).thenReturn(comment);
        when(commentMapper.toCommentReadDto(comment)).thenReturn(commentReadDto);

        CommentReadDto actualDto = commentService.saveComment(VALID_ID, givenDto);

        assertThat(actualDto)
                .isEqualTo(commentReadDto)
                .hasFieldOrPropertyWithValue(Comment.Fields.id, VALID_ID);

    }

    @Test
    void saveComment_shouldThrowPostEntityNotFoundExceptionFail() {
        CommentCreateDto givenDto = new CommentCreateDto("", INVALID_ID);
        when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> commentService.saveComment(INVALID_ID, givenDto))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

        verify(commentMapper, times(0)).toComment(givenDto);
        verify(commentRepository, times(0)).save(comment);
        verify(commentMapper, times(0)).toCommentReadDto(comment);
    }

    @Test
    void updateComment_shouldReturnUpdatedCommentSuccess() {
        CommentReadDto expectedDto = new CommentReadDto(VALID_ID, "updated comment", VALID_ID);
        CommentUpdateDto givenDto = new CommentUpdateDto(VALID_ID, "updated comment", VALID_ID);

        when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        when(commentRepository.findById(VALID_ID)).thenReturn(Optional.of(comment));
        when(commentRepository.update(comment)).thenReturn(comment);
        Mockito.doNothing().when(commentMapper).updateComment(givenDto, comment);
        when(commentMapper.toCommentReadDto(comment)).thenReturn(expectedDto);

        CommentReadDto actualDto = commentService.updateComment(VALID_ID, VALID_ID, givenDto);

        assertThat(actualDto).isEqualTo(expectedDto);
        verify(commentRepository, times(1)).update(comment);
    }

    @Test
    void updateComment_shouldPostThrowEntityNotFoundExceptionFail() {
        CommentUpdateDto givenDto = new CommentUpdateDto(VALID_ID, "", VALID_ID);

        when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);

        assertThatExceptionOfType(EntityNotFoundException.class)
               .isThrownBy(() -> commentService.updateComment(INVALID_ID, INVALID_ID, givenDto))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

        verify(commentMapper, times(0)).updateComment(givenDto, comment);
        verify(commentRepository, times(0)).update(comment);
        verify(commentMapper, times(0)).toCommentReadDto(comment);
    }


    @Test
    void updateComment_shouldCommentThrowEntityNotFoundExceptionFail() {
        CommentUpdateDto givenDto = new CommentUpdateDto(INVALID_ID, "", VALID_ID);

        when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> commentService.updateComment(VALID_ID, INVALID_ID, givenDto))
                .withMessage(messageSource.getMessage(MessageKey.COMMENT_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

        verify(commentMapper, times(0)).updateComment(givenDto, comment);
        verify(commentRepository, times(0)).update(comment);
        verify(commentMapper, times(0)).toCommentReadDto(comment);
    }


    @Test
    void deleteComment_shouldPostThrowEntityNotFoundExceptionFail() {
        when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);


        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> commentService.deleteComment(INVALID_ID, INVALID_ID))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

        verify(commentRepository, times(0)).delete(INVALID_ID);
    }

    @Test
    void findCommentsByPostId_shouldCommentThrowEntityNotFoundExceptionFail() {
        when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        when(commentRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> commentService.deleteComment(VALID_ID, INVALID_ID))
                .withMessage(messageSource.getMessage(MessageKey.COMMENT_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

        verify(commentRepository, times(0)).delete(INVALID_ID);
    }

}
