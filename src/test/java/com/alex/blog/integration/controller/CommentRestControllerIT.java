package com.alex.blog.integration.controller;

import com.alex.blog.api.dto.CommentCreateDto;
import com.alex.blog.api.dto.CommentUpdateDto;
import com.alex.blog.service.MessageKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



class CommentRestControllerIT extends BaseIntegrationTest{

    private final static Long VALID_ID = 1L;
    private final static Long INVALID_ID = 1000000L;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private MockMvc mockMvc;



    @Test
    void getComment_shouldReturnCommentJsonSuccess() throws Exception {

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", VALID_ID, VALID_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.postId").value(VALID_ID));
    }

    @Test
    void getComments_shouldCommentNofFound404Fail() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", VALID_ID, INVALID_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string(messageSource.getMessage(MessageKey.COMMENT_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH)));
    }

    @Test
    void getComments_shouldPostNofFound404Fail() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", INVALID_ID, VALID_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH)));
    }

    @Test
    void getComments_shouldReturnCommentsJsonArraySuccess() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments", VALID_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(VALID_ID))
                .andExpect(jsonPath("$[0].postId").value(VALID_ID));
    }

    @Test
    void getComments_shouldPostNotFound404Fail() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments", INVALID_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH)));
    }

    @Test
    void updateComment_shouldReturnUpdatedCommentJsonSuccess() throws Exception {
        CommentUpdateDto givenDto = new CommentUpdateDto(VALID_ID, "Updated test comment1", VALID_ID);

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", VALID_ID, VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.text").value(givenDto.text()))
                .andExpect(jsonPath("$.postId").value(givenDto.postId()));
    }

    @Test
    void updateComment_shouldCommentNotFound404Fail() throws Exception {
        CommentUpdateDto givenDto=new CommentUpdateDto(INVALID_ID,"Updated test comment1", VALID_ID);

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", VALID_ID, INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(messageSource.getMessage(MessageKey.COMMENT_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH)));
    }

    @Test
    void updateComment_shouldPostNotFound404Fail() throws Exception {
        CommentUpdateDto givenDto=new CommentUpdateDto(INVALID_ID,"Updated test comment1", INVALID_ID);

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", INVALID_ID, INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH)));
    }
    @Test
    void updateComment_shouldPostIdMismatch400Fail() throws Exception {
        CommentCreateDto givenDto = new CommentCreateDto("NEW test comment1", INVALID_ID);

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", VALID_ID, INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @Transactional
    void saveComment_shouldReturnPersistCommentJsonSuccess() throws Exception {
        CommentCreateDto givenDto = new CommentCreateDto("NEW test comment1", VALID_ID);

        mockMvc.perform(post("/api/posts/{postId}/comments", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.text").value(givenDto.text()))
                .andExpect(jsonPath("$.postId").value(givenDto.postId()));
    }

    @Test
    void saveComment_shouldMismatchPostId400Fail() throws Exception {
        CommentCreateDto givenDto = new CommentCreateDto("NEW test comment1", VALID_ID);

        mockMvc.perform(post("/api/posts/{postId}/comments", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void saveComment_shouldPostNotFoundFail() throws Exception {
        CommentCreateDto givenDto = new CommentCreateDto("NEW test comment1", INVALID_ID);

        mockMvc.perform(post("/api/posts/{postId}/comments", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH)));
    }


    @Test
    void delete_shouldDeleteCommentSuccess() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}/comments/{commId}", VALID_ID, VALID_ID))
                .andExpect(status().isOk());

        Boolean existsComment = jdbcTemplate.queryForObject("SELECT EXISTS (SELECT 1 FROM comments WHERE id = ?)", Boolean.class, VALID_ID);

        Assertions.assertThat(existsComment).isFalse();
    }

    @Test
    void delete_shouldDeleteCommentCommentNotFound404Fail() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}/comments/{commId}", VALID_ID, INVALID_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string(messageSource.getMessage(MessageKey.COMMENT_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH)));
    }

    @Test
    void delete_shouldDeletePostNotFound404Fail() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}/comments/{commId}", INVALID_ID, VALID_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH)));
    }

}