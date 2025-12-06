package com.alex.blog.api.rest.controller;

import com.alex.blog.WebConfiguration;
import com.alex.blog.api.dto.PostReadDto;
import com.alex.blog.model.Post;
import com.alex.blog.search.PostPageDto;
import com.alex.blog.search.SearchDto;
import com.alex.blog.service.CommentService;
import com.alex.blog.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.TestDataSourceConfig;
import config.TestWebConfig;
import lombok.SneakyThrows;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
//org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WebConfiguration.class, TestDataSourceConfig.class})

@WebAppConfiguration
class PostRestControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PostService postService;

  //  @Autowired
  //  private ObjectMapper objectMapper;

    private MockMvc mockMvc;

   /* @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }*/
    @BeforeEach
   void setUp() {
       // Настраиваем MockMvc с поддержкой multipart
       mockMvc = MockMvcBuilders
               .webAppContextSetup(webApplicationContext)
               .addFilter(new CharacterEncodingFilter("UTF-8", true))
               .alwaysDo(print())
               .build();
   }





    @Test
    void findById() {

    }

    @Test
    void create() {
    }

    @Test
    void update() {
    }



    @SneakyThrows
     @ParameterizedTest
    @MethodSource("getArgsForSearch")
    void givenTagsOrAndTitle_search_shouldReturnJSONArray(String paramSearch,String pageSize, int sizeArray,String hasPrev, String hasNext, String lastPage) {
        // SearchDto searchDto = new SearchDto("test title1 , 1, 3);
        PostReadDto postReadDto = new PostReadDto(1L, "test title1", "test desc1", List.of("test_tag1"), 2L, 3L);

        // Mockito.when(postService.findPageByCriteria(searchDto)).thenReturn(new PostPageDto(List.of(postReadDto),false,true,1));
        mockMvc.perform(get("/api/posts")
                        .param("search", paramSearch)
                        .param("pageNumber", "1")
                        .param("pageSize", pageSize)
                )
                .andExpect(status().isOk())
                .andExpect((content().contentType(MediaType.APPLICATION_JSON)))
                .andExpect(jsonPath("$.posts", hasSize(sizeArray)))
                .andExpect(jsonPath("$.posts[0].title").value("test title1"))
                .andExpect(jsonPath("$.posts[0].id").value("1"))
                .andExpect(jsonPath("$.posts[1].title").value("test title2"))
                .andExpect(jsonPath("$.posts[1].id").value("2"))
                .andExpect(jsonPath("$.hasPrev").value(hasPrev))
                .andExpect(jsonPath("$.hasNext").value(hasNext))
                .andExpect(jsonPath("$.lastPage").value(lastPage));
    }

    public static Stream<Arguments> getArgsForSearch(){

        return Stream.of(
                Arguments.of("#test_tag1","3",2,"false","false","0"),
                Arguments.of("test t #test_tag1","3",2,"false","false","0"),
                Arguments.of("test t","3",3,"false","false","0"),
                Arguments.of("test t","2",2,"false","true","1")

        );
    }


    @Test
    void updateImage() throws Exception {

    }



    @Test
    void getImage() throws Exception {
        byte[] givenImage = new byte[]{(byte) 137, 80, 78, 71};
        MockMultipartFile file = new MockMultipartFile("image", "image.jpg", "image/jpg", givenImage);

        // Создаем специальный builder для PUT multipart
    /*    MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/api/posts/{postId}/image", 1);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });*/

        mockMvc.perform(multipart(HttpMethod.PUT,"/api/posts/{id}/image",1)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{id}/image", 1L))
                .andExpect(status().isOk())
               // .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().bytes(givenImage));
    }

    @Test
    void incrementLikesCount() {
    }

    @Test
    void deletePostWithComments() {
    }
}