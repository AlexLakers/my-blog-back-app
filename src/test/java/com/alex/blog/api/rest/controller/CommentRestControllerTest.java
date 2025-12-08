package com.alex.blog.api.rest.controller;

import com.alex.blog.WebConfiguration;
import com.alex.blog.model.Comment;
import com.alex.blog.model.Post;
import com.alex.blog.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.ObjectMapperConfig;
import config.TestDataSourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WebConfiguration.class, TestDataSourceConfig.class, ObjectMapperConfig.class})

@WebAppConfiguration
@TestPropertySource(locations = "classpath:application-test.properties")
class CommentRestControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

  /*  @Autowired
    private PostService postService;*/

    private static final Comment comment = new Comment(1L, "test comment1", 1L);
    private final static Long VALID_ID=1L;
    private final static Long INVALID_ID=1000000L;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
    }

    @Test
    void getComment_shouldReturnCommentJsonSucess() throws Exception {

            Post expectedPost = new Post(1L, "test title1", "test desc1", List.of("test_tag1"), "1/image.jpg", 2L, 3L);

            mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", VALID_ID,VALID_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                    .andExpect(jsonPath("$.text").value(comment.getText()))
                    .andExpect(jsonPath("$.id").value(VALID_ID))
                    .andExpect(jsonPath("$.postId").value(VALID_ID));
        }

        @Test
        void getComments_shouldCommentNofFound404Fail() throws Exception {
            mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", VALID_ID,INVALID_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=ISO-8859-1")))
                    .andExpect(content().string("The comment not found by id:%d".formatted(INVALID_ID)));
        }
    @Test
    void getComments_shouldPostNofFound404Fail() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", INVALID_ID,VALID_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=ISO-8859-1")))
                .andExpect(content().string("The post not found by id:%d".formatted(INVALID_ID)));
    }

    @Test
    void getComments_shouldReturnCommentsJsonArraySuccess() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments", VALID_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(VALID_ID))
                .andExpect(jsonPath("$[0].text").value(comment.getText()))
                .andExpect(jsonPath("$[0].postId").value(VALID_ID));
    }
    @Test
    void getComments_shouldPostNotFound404Fail() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments", INVALID_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=ISO-8859-1")))
                .andExpect(content().string("The post not found by id:%d".formatted(INVALID_ID)));
    }

    @Test
    @Transactional
    void updateComment_shouldReturnUpdatedCommentJsonSuccess() throws Exception {
         Comment expectedComment = new Comment(VALID_ID, "Updated test comment1", 2L);

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}",VALID_ID,VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedComment))
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.text").value(expectedComment.getText()))
                .andExpect(jsonPath("$.postId").value(expectedComment.getPostId()));
    }
    @Test
    void updateComment_shouldCommentNotFound404Fail() throws Exception {

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}",VALID_ID,INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment))
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                 .andExpect(content().string("The comment not found by id:%d".formatted(INVALID_ID)));
    }
    @Test
    void updateComment_shouldPostNotFound404Fail() throws Exception {

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}",INVALID_ID,VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment))
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().string("The post not found by id:%d".formatted(INVALID_ID)));
    }




    @Test
    void delete() {
    }
}