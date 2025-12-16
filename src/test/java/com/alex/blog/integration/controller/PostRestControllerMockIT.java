package com.alex.blog.integration.controller;

import com.alex.blog.api.dto.PostCreateDto;
import com.alex.blog.api.dto.PostReadDto;
import com.alex.blog.api.dto.PostUpdateDto;
import com.alex.blog.api.rest.controller.PostRestController;
import com.alex.blog.exception.EntityNotFoundException;
import com.alex.blog.exception.TitleAlreadyExistsException;
import com.alex.blog.search.PostPageDto;
import com.alex.blog.search.SearchDto;
import com.alex.blog.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostRestController.class)
@ActiveProfiles("test")
public class PostRestControllerMockIT {
    private static final Long VALID_ID = 1L;
    private static final Long INVALID_ID = 10000L;

    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean(reset = MockReset.BEFORE)
    private PostService postService;
    @Autowired
    private MockMvc mockMvc;


    @Test
    void findById_shouldReturnPostJsonSuccess() throws Exception {
        PostReadDto expectedPost = new PostReadDto(1L, "test title1", "test desc1", List.of("test_tag1"), 2L, 3L);
        when(postService.findOnePost(VALID_ID)).thenReturn(expectedPost);

        mockMvc.perform(get("/api/posts/{postId}", VALID_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value(expectedPost.title()))
                .andExpect(jsonPath("$.id").value(expectedPost.id()))
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0]", is(expectedPost.tags().get(0))));
    }


    @Test
    void findById_shouldNotFound404() throws Exception {
        doThrow(EntityNotFoundException.class).when(postService).findOnePost(INVALID_ID);

        mockMvc.perform(get("/api/posts/{postId}", INVALID_ID))
                .andExpect(status().isNotFound());
    }


    @Test
    void create_shouldReturnCreatedJson() throws Exception {
        PostCreateDto newPost = new PostCreateDto("test title100", "description", List.of("newCreateTag"));
        PostReadDto expectedPost = new PostReadDto(VALID_ID, newPost.title(),newPost.text(), newPost.tags(), 0L, 0L);
        when(postService.savePost(newPost)).thenReturn(expectedPost);

    mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPost))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.title").value(expectedPost.title()));
    }

    @Test
    void create_shouldTitleAlreadyExistBadRequest400() throws Exception {
        PostCreateDto givenDto = new PostCreateDto("test title1", "description", List.of("newCreateTag"));
        doThrow(TitleAlreadyExistsException.class).when(postService).savePost(givenDto);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void update_shouldReturnUpdatedJsonSuccess() throws Exception {
        PostUpdateDto givenDto = new PostUpdateDto(VALID_ID, "testTitle", "testText", List.of("test_tag1"));
        PostReadDto expectedPost = new PostReadDto(VALID_ID, givenDto.title(), givenDto.text(), givenDto.tags(), 2L, 3L);

        when(postService.updatePost(VALID_ID,givenDto)).thenReturn(expectedPost);

        mockMvc.perform(put("/api/posts/{postId}", VALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(VALID_ID))
                .andExpect(jsonPath("$.title").value(expectedPost.title()))
                .andExpect(jsonPath("$.id").value(expectedPost.id()))
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0]", is(expectedPost.tags().get(0))));
    }

    @Test
    void update_shouldReturnPostNotFound404() throws Exception {
        PostUpdateDto givenDto = new PostUpdateDto(VALID_ID, "testTitle", "testText", null);

        doThrow(EntityNotFoundException.class).when(postService).updatePost(INVALID_ID, givenDto);

        mockMvc.perform(put("/api/posts/{postId}", INVALID_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(givenDto)))
                .andExpect(status().isNotFound());

    }


    @Test
    void givenTagsOrAndTitle_search_shouldReturnJSONArray() throws Exception {
        SearchDto givenDto=new SearchDto("title",1,3);
        PostReadDto expectedPostOne = new PostReadDto(VALID_ID, null, null, null,null,null);
        PostReadDto expectedPostTwo = new PostReadDto(INVALID_ID, null, null, null,null,null);
        PostPageDto expectedDto=new PostPageDto(List.of(expectedPostOne,expectedPostTwo),false,false,1);
        when(postService.findPageByCriteria(givenDto))
                        .thenReturn(expectedDto);

        mockMvc.perform(get("/api/posts")
                        .param("search", String.valueOf(givenDto.search()))
                        .param("pageNumber", String.valueOf(givenDto.pageNumber()))
                        .param("pageSize", String.valueOf(givenDto.pageSize()))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(2)))
                .andExpect(jsonPath("$.posts[0].id").value(VALID_ID))
                .andExpect(jsonPath("$.posts[1].id").value(INVALID_ID))
                .andExpect(jsonPath("$.hasPrev").value(expectedDto.hasPrev()))
                .andExpect(jsonPath("$.hasNext").value(expectedDto.hasNext()))
                .andExpect(jsonPath("$.lastPage").value(expectedDto.lastPage()));
    }



    @Test
    void updateImage_ShouldPostNotFound404() throws Exception {
        MockMultipartFile image =new MockMultipartFile("image",new byte[]{1, 2, 3, 4});
    doThrow(EntityNotFoundException.class).when(postService).updateImage(INVALID_ID,image);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", INVALID_ID)
                .file(image)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound());
    }


    @Test
    void uploadAndDownloadSuccess_shouldSaveAndReturnArrayBytes() throws Exception {
        byte[] givenImage = new byte[]{(byte) 137, 80, 78, 71};
        MockMultipartFile file = new MockMultipartFile("image", "image.jpg", "image/jpg", givenImage);
        when(postService.updateImage(VALID_ID,file)).thenReturn(true);
        when(postService.getImage(VALID_ID)).thenReturn(givenImage);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", 1)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{id}/image", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().bytes(givenImage));
    }

    @Test
    void incrementLikesCount_shouldReturnStringLikesCount() throws Exception {
        when(postService.incrementLikesCount(VALID_ID)).thenReturn(3L);

        mockMvc.perform(post("/api/posts/{id}/likes", VALID_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void deletePostWithComments_shouldDeletePostsRWithCommentsSuccess() throws Exception {
        doNothing().when(postService).deletePost(VALID_ID);

        mockMvc.perform(delete("/api/posts/{postId}", VALID_ID))
                .andExpect(status().isOk());
    }

    @Test
    void deletePostWithComments_shouldPostNotFound404Fail() throws Exception {
        doThrow(EntityNotFoundException.class).when(postService).deletePost(INVALID_ID);

        mockMvc.perform(delete("/api/posts/{postId}", INVALID_ID))
                .andExpect(status().isNotFound());
    }
}
