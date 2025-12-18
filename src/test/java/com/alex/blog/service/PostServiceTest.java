package com.alex.blog.service;

import com.alex.blog.api.dto.PostCreateDto;
import com.alex.blog.api.dto.PostReadDto;
import com.alex.blog.api.dto.PostUpdateDto;
import com.alex.blog.exception.EntityNotFoundException;
import com.alex.blog.exception.TitleAlreadyExistsException;
import com.alex.blog.mapper.PostMapper;
import com.alex.blog.model.Post;
import com.alex.blog.repository.CommentRepository;
import com.alex.blog.repository.PostManagementRepository;
import com.alex.blog.repository.PostSearchRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class PostServiceTest {

    @MockitoBean
    private PostManagementRepository postManagementRepository;
    @MockitoBean
    private PostSearchRepository postSearchRepository;
    @MockitoBean
    private PostMapper postMapper;
    @MockitoBean
    CommentRepository commentRepository;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private PostService postService;

    private final static Long VALID_ID = 1L;
    private final static Long INVALID_ID = 10000L;
    private final static Post post = new Post(1L, "test title1", "test desc1", List.of("test_tag1"), new byte[]{1,2,3,4}, 2L, 3L);
    PostReadDto expectedDto = new PostReadDto(1L, "test title1", "test desc1", List.of("test_tag1"), 2L, 3L);

    @Test
    void findOnePost_shouldReturnPostSuccess() {
        when(postSearchRepository.findPostById(VALID_ID)).thenReturn(Optional.of(post));
        when(postMapper.toPostReadDto(post)).thenReturn(expectedDto);

        PostReadDto actualDto = postService.findOnePost(VALID_ID);

        assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void findOnePost_shouldPostThrowEntityNotFoundException() {
        when(postSearchRepository.findPostById(INVALID_ID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> postService.findOnePost(INVALID_ID))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));
    }


    @Test
    void incrementLikesCount_shouldReturnIncLikesCountSuccess() {
        Long expectedLikes = 100L;
        when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        when(postManagementRepository.incrementLikesCount(VALID_ID)).thenReturn(expectedLikes);

        Long actualLikes = postService.incrementLikesCount(VALID_ID);

        assertThat(actualLikes).isEqualTo(expectedLikes);
    }

    @Test
    void incrementLikesCount_shouldPostThrowEntityNotFoundException() {
        when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> postService.incrementLikesCount(INVALID_ID))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));
    }

    @Test
    void findPageByCriteria() {
    }

    @Test
    void updatePost_shouldReturnUpdatedPostSuccess() {
        PostUpdateDto givenDto = new PostUpdateDto(VALID_ID, "testTitle", "testText", List.of("test_tag1"));
        when(postSearchRepository.findPostById(VALID_ID)).thenReturn(Optional.of(post));
        doNothing().when(postMapper).updatePost(givenDto, post);
        when(postManagementRepository.update(Mockito.any(Post.class))).thenReturn(post);
        when(postMapper.toPostReadDto(post)).thenReturn(expectedDto);

        PostReadDto actualDto = postService.updatePost(VALID_ID, givenDto);

        assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void updatePost_shouldPostThrowEntityNotFoundExceptionFail() {
        PostUpdateDto givenDto = new PostUpdateDto(INVALID_ID, "testTitle", "testText", List.of("test_tag1"));
        when(postSearchRepository.findPostById(INVALID_ID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> postService.updatePost(INVALID_ID, givenDto))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

    }


    @Test
    void savePost_shouldReturnPersistentPostSuccess() {
        PostReadDto expectedDto = new PostReadDto(1L, "testTitle1", "test desc1", List.of("test_tag1"), 2L, 3L);
        PostCreateDto givenDto = new PostCreateDto("testTitle", "test text", List.of("test_tag1"));
        when(postManagementRepository.existsByTitle(givenDto.title())).thenReturn(false);
        when(postMapper.toPost(Mockito.any(PostCreateDto.class))).thenReturn(post);
        when(postManagementRepository.save(Mockito.any(Post.class))).thenReturn(post);
        when(postMapper.toPostReadDto(post)).thenReturn(expectedDto);

        PostReadDto actualDto = postService.savePost(givenDto);

        assertThat(actualDto).isEqualTo(expectedDto);

        verify(postManagementRepository, Mockito.times(1)).save(post);
        verify(postMapper, Mockito.times(1)).toPostReadDto(post);

    }

    @Test
    void savePost_shouldThrowTitleAlreadyExistsExceptionFail() {
        PostCreateDto givenDto = new PostCreateDto("testTitle", "test text", List.of("test_tag1"));
        when(postManagementRepository.existsByTitle(givenDto.title())).thenReturn(true);

        assertThatExceptionOfType(TitleAlreadyExistsException.class)
                .isThrownBy(() -> postService.savePost(givenDto))
                .withMessage(messageSource.getMessage(MessageKey.POST_TITLE_EXISTS_EX, new Object[]{givenDto.title()}, Locale.ENGLISH));

        verify(postManagementRepository, Mockito.times(0)).save(post);
        verify(postMapper, Mockito.times(0)).toPostReadDto(post);
    }

    @Test
    void deletePost_shouldCallDeletePostSuccess() {
        when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        doNothing().when(postManagementRepository).delete(VALID_ID);
        doNothing().when(commentRepository).deleteByPostId(VALID_ID);

        postService.deletePost(VALID_ID);

        verify(postManagementRepository, Mockito.times(1)).delete(VALID_ID);
        verify(commentRepository, Mockito.times(1)).deleteByPostId(VALID_ID);

    }

    @Test
    void deletePost_shouldThrowEntityNotFoundException() {
        when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);


        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> postService.deletePost(INVALID_ID))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

        verify(postManagementRepository, Mockito.times(0)).delete(INVALID_ID);
        verify(commentRepository, Mockito.times(0)).deleteByPostId(INVALID_ID);


    }

    @Test
    void getImage_shouldReturnArrayBytesSuccess() {
        byte[] expectedImage = new byte[]{1, 2, 3, 4};
        when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        when(postManagementRepository.getImage(VALID_ID)).thenReturn(Optional.of(expectedImage));


        byte[] image = postService.getImage(VALID_ID);

        assertThat(image).isEqualTo(expectedImage);
    }
    @Test
    void getImage_shouldThrowEntityNotFoundExceptionFail() {
        when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> postService.getImage(INVALID_ID))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

        verify(postManagementRepository, Mockito.times(0)).getImage(INVALID_ID);
    }

    @Test
    void updateImage_shouldCallSaveFileAndUpdateImageSuccess() throws IOException {
        MultipartFile image =new MockMultipartFile("image",new byte[]{1, 2, 3, 4});
        when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        when(postManagementRepository.updateImage(VALID_ID,image.getBytes())).thenReturn(true);

        postService.updateImage(VALID_ID,image);

        verify(postManagementRepository,Mockito.times(1)).updateImage(VALID_ID,image.getBytes());
    }
}