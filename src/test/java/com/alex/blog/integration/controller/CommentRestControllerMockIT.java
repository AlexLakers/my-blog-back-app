package com.alex.blog.integration.controller;

import com.alex.blog.api.dto.CommentCreateDto;
import com.alex.blog.api.dto.CommentReadDto;
import com.alex.blog.api.dto.CommentUpdateDto;
import com.alex.blog.api.rest.controller.CommentRestController;
import com.alex.blog.exception.EntityNotFoundException;
import com.alex.blog.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;


import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentRestController.class)
public class CommentRestControllerMockIT {
    private final static Long VALID_ID = 1L;
    private final static Long INVALID_ID = 1000000L;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean(reset = MockReset.BEFORE)
    private CommentService commentService;


    @Test
    void getComment_shouldReturnCommentJsonSuccess() throws Exception {
        CommentReadDto expectedDto = new CommentReadDto(VALID_ID, "text comment", VALID_ID);
        when(commentService.findOneComment(VALID_ID,VALID_ID)).thenReturn(expectedDto);

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", VALID_ID, VALID_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.postId").value(VALID_ID));
    }

    @Test
    void getComments_shouldCommentNofFound404Fail() throws Exception {
        doThrow(EntityNotFoundException.class).when(commentService).findOneComment(VALID_ID, INVALID_ID);

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", VALID_ID, INVALID_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getComments_shouldPostNofFound404Fail() throws Exception {
        doThrow(EntityNotFoundException.class).when(commentService).findOneComment(INVALID_ID, VALID_ID);

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", INVALID_ID, VALID_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getComments_shouldReturnCommentsJsonArraySuccess() throws Exception {
        List<CommentReadDto> expectedDtos = List.of( new CommentReadDto(VALID_ID, "text comment", VALID_ID));
        when(commentService.findCommentsByPostId(VALID_ID)).thenReturn(expectedDtos);

        mockMvc.perform(get("/api/posts/{postId}/comments", VALID_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(VALID_ID))
                .andExpect(jsonPath("$[0].postId").value(VALID_ID));
    }

    @Test
    void getComments_shouldPostNotFound404Fail() throws Exception {
        doThrow(EntityNotFoundException.class).when(commentService).findCommentsByPostId(INVALID_ID);

        mockMvc.perform(get("/api/posts/{postId}/comments", INVALID_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateComment_shouldReturnUpdatedCommentJsonSuccess() throws Exception {
        CommentUpdateDto givenDto = new CommentUpdateDto(VALID_ID, "Updated test comment1", VALID_ID);
        CommentReadDto expectedDto = new CommentReadDto(VALID_ID, givenDto.text(), VALID_ID);
        when(commentService.updateComment(VALID_ID,VALID_ID, givenDto)).thenReturn(expectedDto);

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", VALID_ID, VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.text").value(expectedDto.text()))
                .andExpect(jsonPath("$.postId").value(expectedDto.postId()));
    }

    @Test
    void updateComment_shouldCommentNotFound404Fail() throws Exception {
        CommentUpdateDto givenDto = new CommentUpdateDto(INVALID_ID, "Updated test comment1", VALID_ID);
        doThrow(EntityNotFoundException.class).when(commentService).updateComment(VALID_ID,INVALID_ID, givenDto);

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", VALID_ID, INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateComment_shouldPostNotFound404Fail() throws Exception {
        CommentUpdateDto givenDto = new CommentUpdateDto(INVALID_ID, "Updated test comment1", INVALID_ID);
        doThrow(EntityNotFoundException.class).when(commentService).updateComment(INVALID_ID,INVALID_ID, givenDto);

                mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", INVALID_ID, INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto)))
                .andExpect(status().isNotFound());
    }


    @Test
    void saveComment_shouldReturnPersistCommentJsonSuccess() throws Exception {
        CommentCreateDto givenDto = new CommentCreateDto("NEW test comment1", VALID_ID);
        CommentReadDto expectedDto = new CommentReadDto(VALID_ID, givenDto.text(), VALID_ID);
        when(commentService.saveComment(VALID_ID, givenDto)).thenReturn(expectedDto);

        mockMvc.perform(post("/api/posts/{postId}/comments", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.text").value(expectedDto.text()))
                .andExpect(jsonPath("$.postId").value(expectedDto.postId()));
    }


    @Test
    void saveComment_shouldPostNotFoundFail() throws Exception {
        CommentCreateDto givenDto = new CommentCreateDto("NEW test comment1", INVALID_ID);
        doThrow(EntityNotFoundException.class).when(commentService).saveComment(INVALID_ID, givenDto);

        mockMvc.perform(post("/api/posts/{postId}/comments", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto)))
                .andExpect(status().isNotFound());
    }


    @Test
    void delete_shouldDeleteCommentSuccess() throws Exception {
        doNothing().when(commentService).deleteComment(VALID_ID, VALID_ID);

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commId}", VALID_ID, VALID_ID))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldDeleteCommentCommentNotFound404Fail() throws Exception {
        doThrow(EntityNotFoundException.class).when(commentService).deleteComment(VALID_ID, INVALID_ID);

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commId}", VALID_ID, INVALID_ID))
                .andExpect(status().isNotFound());
    }
}