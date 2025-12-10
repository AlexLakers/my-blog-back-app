package com.alex.blog.mapper;

import com.alex.blog.api.dto.PostCreateDto;
import com.alex.blog.api.dto.PostReadDto;
import com.alex.blog.api.dto.PostUpdateDto;
import com.alex.blog.config.TestMapperConfig;
import com.alex.blog.model.Post;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;

@ContextConfiguration(classes = TestMapperConfig.class)
@ExtendWith(SpringExtension.class)
class PostMapperTest {
    private final static Post post = new Post(null, "test title1", "test desc1", List.of("test_tag1"), null, 0L, 0L);
    @Autowired
    public PostMapper postMapper;

    @Test
    void toPost_shouldReturnPost() {
        PostCreateDto givenDto = new PostCreateDto( "test title1", "test desc1", List.of("test_tag1"));

        Post actual=postMapper.toPost(givenDto);

        Assertions.assertThat(actual).isEqualTo(post);
    }

    @Test
    void updatePost_shouldSetFieldsToPost() {
        PostUpdateDto givenDto = new PostUpdateDto(1L, "UPDATED", "UPDATED", null);

        postMapper.updatePost(givenDto,post);

        Assertions.assertThat(post)
                .hasFieldOrPropertyWithValue(Post.Fields.title,givenDto.title())
                .hasFieldOrPropertyWithValue(Post.Fields.text,givenDto.text());
    }

    @Test
    void toPostReadDto_shouldReturnPostReadDto() {
        PostReadDto expectedDto = new PostReadDto(null, "test title1", "test desc1", List.of("test_tag1"), 0L, 0L);

        PostReadDto actualDto = postMapper.toPostReadDto(post);

        Assertions.assertThat(actualDto).isEqualTo(expectedDto);
    }
}