package com.alex.blog.api.rest.controller;

import com.alex.blog.WebConfiguration;
import com.alex.blog.api.dto.PostReadDto;
import com.alex.blog.model.Post;
import com.alex.blog.search.PostPageDto;
import com.alex.blog.search.SearchDto;
import com.alex.blog.service.CommentService;
import com.alex.blog.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.ObjectMapperConfig;
import config.TestDataSourceConfig;
import config.TestWebConfig;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
//org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WebConfiguration.class, TestDataSourceConfig.class, ObjectMapperConfig.class})

@WebAppConfiguration
@TestPropertySource(locations = "classpath:application-test.properties")
class PostRestControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PostService postService;

    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 10000L;

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
    void findById_shouldReturnPostJsonSuccess() throws Exception {
        Post expectedPost = new Post(1L, "test title1", "test desc1", List.of("test_tag1"), "1/image.jpg", 2L, 3L);

        mockMvc.perform(get("/api/posts/{postId}", VALID_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.title").value(expectedPost.getTitle()))
                .andExpect(jsonPath("$.id").value(expectedPost.getId()))
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0]", is(expectedPost.getTags().get(0))));
    }


    @Test
    void findById_shouldNotFound404() throws Exception {

        mockMvc.perform(get("/api/posts/{postId}", INVALID_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("The post not found by id:%d".formatted(INVALID_ID)));
    }



    @Test
    void create_shouldReturnCreatedJson() throws Exception {
        Post newPost = new Post(null, "newCreateTitle", "description", List.of("newCreateTag"), "new/path", 0L, 0L);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPost))
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.valueOf("application/json;charset=UTF-8")))
                .andExpect(jsonPath("$.id").value(4L))
                .andExpect(jsonPath("$.title").value(newPost.getTitle()));
    }

    @Test
    void create_shouldTitleAlreadyExistBadRequest400() throws Exception {
        Post newPost = new Post(null, "test title1", "description", List.of("newCreateTag"), "new/path", 0L, 0L);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPost))
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.valueOf("application/json;charset=UTF-8")))
                .andExpect(content().string("The title: %s already exists".formatted(newPost.getTitle())));
    }


    @Test
    @Transactional
    void update_shouldReturnUpdatedJsonSuccess() throws Exception {
        Post expectedPost = new Post(VALID_ID, "newUpdateTitle", "description", List.of("newUpdateTag"), "new/path", 1L, 1L);

        mockMvc.perform(put("/api/posts/{postId}",VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedPost))
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/json;charset=UTF-8")))
                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.title").value(expectedPost.getTitle()))
                .andExpect(jsonPath("$.id").value(expectedPost.getId()))
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0]", is(expectedPost.getTags().get(0))));
    }

    @Test
    void update_shouldReturnPostNotFound404() throws Exception {
        Post expectedPost = new Post();
        expectedPost.setId(INVALID_ID);

        mockMvc.perform(put("/api/posts/{postId}", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedPost))
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("application/json;charset=UTF-8")))
                .andExpect(content().string("The post not found by id:%d".formatted(INVALID_ID)));
    }


    @SneakyThrows
    @ParameterizedTest
    @MethodSource("getArgsForSearch")
    void givenTagsOrAndTitle_search_shouldReturnJSONArray(String paramSearch, String pageSize, int sizeArray, String hasPrev, String hasNext, String lastPage) {
        PostReadDto postReadDto = new PostReadDto(1L, "test title1", "test desc1", List.of("test_tag1"), 2L, 3L);

        // Mockito.when(postService.findPageByCriteria(searchDto)).thenReturn(new PostPageDto(List.of(postReadDto),false,true,1));
        mockMvc.perform(get("/api/posts")
                        .param("search", paramSearch)
                        .param("pageNumber", "1")
                        .param("pageSize", pageSize)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("application/json;charset=UTF-8")))
                .andExpect(jsonPath("$.posts", hasSize(sizeArray)))
                .andExpect(jsonPath("$.posts[0].title").value("test title1"))
                .andExpect(jsonPath("$.posts[0].id").value("1"))
                .andExpect(jsonPath("$.posts[1].title").value("test title2"))
                .andExpect(jsonPath("$.posts[1].id").value("2"))
                .andExpect(jsonPath("$.hasPrev").value(hasPrev))
                .andExpect(jsonPath("$.hasNext").value(hasNext))
                .andExpect(jsonPath("$.lastPage").value(lastPage));
    }

    public static Stream<Arguments> getArgsForSearch() {

        return Stream.of(
                Arguments.of("#test_tag1", "3", 2, "false", "false", "0"),
                Arguments.of("test t #test_tag1", "3", 2, "false", "false", "0"),
                Arguments.of("test t", "3", 3, "false", "false", "0"),
                Arguments.of("test t", "2", 2, "false", "true", "1")

        );
    }


    @Test

    void updateImage_ShouldImageNotFound404() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/image", VALID_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("The image path or image not found for post with id:%d".formatted(VALID_ID)));
    }

    @Test

    void updateImage_ShouldPostNotFound404() throws Exception {


        mockMvc.perform(get("/api/posts/{id}/image", INVALID_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("The post with id:%1$d not found".formatted(INVALID_ID)));
    }


    @Test
    @Transactional
    void uploadAndDownloadSuccess_shouldSaveAndReturnArrayBytes() throws Exception {
        byte[] givenImage = new byte[]{(byte) 137, 80, 78, 71};
        MockMultipartFile file = new MockMultipartFile("image", "image.jpg", "image/jpg", givenImage);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", 1)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{id}/image", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.valueOf("image/jpeg;charset=UTF-8")))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().bytes(givenImage));
    }

    @Test
    @Transactional
    void incrementLikesCount_shouldReturnStringLikesCount() throws Exception {
        mockMvc.perform(post("/api/posts/{id}/likes", VALID_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    @Transactional
    void deletePostWithComments_shouldDeletePostsRWithCommentsSuccess() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}", VALID_ID))
                .andExpect(status().isOk());

        Boolean existsPosts=jdbcTemplate.queryForObject("SELECT EXISTS (SELECT 1 FROM posts WHERE id = ?)", Boolean.class, VALID_ID);
        Boolean existsComments=jdbcTemplate.queryForObject("SELECT EXISTS (SELECT 1 FROM comments WHERE post_id = ?)", Boolean.class, VALID_ID);

        Assertions.assertThat(existsPosts).isFalse();
        Assertions.assertThat(existsComments).isFalse();
    }

    @Test
    void deletePostWithComments_shouldPostNotFound404Fail() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}", INVALID_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("The post not found by id:%d".formatted(INVALID_ID)));
    }

}