package com.alex.blog.service;

import com.alex.blog.api.dto.PostReadDto;
import com.alex.blog.exception.EntityNotFoundException;
import com.alex.blog.mapper.CommentMapper;
import com.alex.blog.mapper.PostMapper;
import com.alex.blog.model.Post;
import com.alex.blog.repository.CommentRepository;
import com.alex.blog.repository.PostManagementRepository;
import com.alex.blog.repository.PostSearchRepository;
import com.alex.blog.service.impl.CommentServiceImpl;
import com.alex.blog.service.impl.FileServiceImpl;
import com.alex.blog.service.impl.PostServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostManagementRepository postManagementRepository;
    @Mock
    private PostSearchRepository postSearchRepository;
    @Mock
    private PostMapper postMapper;
    @Mock
    private FileService fileService;

    @InjectMocks
    private PostServiceImpl postService;

    private final static Long VALID_ID = 1L;
    private final static Long INVALID_ID = 10000L;
    private final static Post post = new Post(1L, "test title1", "test desc1", List.of("test_tag1"), "1/image.jpg", 2L, 3L);

    @Test
    void findOnePost_shouldReturnPostSuccess() {
        PostReadDto expectedDto=new PostReadDto(1L, "test title1", "test desc1", List.of("test_tag1"), 2L, 3L);
        Mockito.when(postSearchRepository.findPostById(VALID_ID)).thenReturn(Optional.of(post));
        Mockito.when(postMapper.toPostReadDto(post)).thenReturn(expectedDto);

        PostReadDto actualDto=postService.findOnePost(VALID_ID);

        Assertions.assertThat(actualDto).isEqualTo(expectedDto);
    }
    @Test
    void findOnePost_shouldPostThrowEntityNotFoundException() {
        Mockito.when(postSearchRepository.findPostById(INVALID_ID)).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> postService.findOnePost(INVALID_ID))
                .withMessage("The post not found by id:%d".formatted(INVALID_ID));
    }


    @Test
    void incrementLikesCount_shouldReturnIncLikesCountSuccess() {
        Long expectedLikes=100L;
        Mockito.when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        Mockito.when(postManagementRepository.incrementLikesCount(VALID_ID)).thenReturn(expectedLikes);

        Long actualLikes= postService.incrementLikesCount(VALID_ID);

        Assertions.assertThat(actualLikes).isEqualTo(expectedLikes);
    }

    @Test
    void incrementLikesCount_shouldPostThrowEntityNotFoundException() {
        Mockito.when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);

        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> postService.incrementLikesCount(INVALID_ID))
                .withMessage("The post not found by id:%d".formatted(INVALID_ID));
    }

    @Test
    void findPageByCriteria() {
    }

    @Test
    void updatePost() {
    }

    @Test
    void savePost() {
    }

    @Test
    void deletePost() {
    }

    @Test
    void getImage() {
    }

    @Test
    void updateImage() {
    }
}