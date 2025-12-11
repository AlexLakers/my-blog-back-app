package com.alex.blog.service;

import com.alex.blog.api.dto.PostCreateDto;
import com.alex.blog.api.dto.PostReadDto;
import com.alex.blog.api.dto.PostUpdateDto;
import com.alex.blog.config.TestServiceConfig;
import com.alex.blog.exception.EntityNotFoundException;
import com.alex.blog.exception.TitleAlreadyExistsException;
import com.alex.blog.mapper.PostMapper;
import com.alex.blog.model.Post;
import com.alex.blog.repository.CommentRepository;
import com.alex.blog.repository.PostManagementRepository;
import com.alex.blog.repository.PostSearchRepository;
import com.alex.blog.service.impl.PostServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.Mockito.reset;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestServiceConfig.class)
class PostServiceTest {

    @Autowired
    private PostManagementRepository postManagementRepository;
    @Autowired
    private PostSearchRepository postSearchRepository;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private PostService postService;

    @BeforeEach
    void resetMocks() {
        reset(postManagementRepository,commentRepository,postSearchRepository,postMapper,fileService);
    }


    private final static Long VALID_ID = 1L;
    private final static Long INVALID_ID = 10000L;
    private final static Post post = new Post(1L, "test title1", "test desc1", List.of("test_tag1"), "1/image.jpg", 2L, 3L);
    PostReadDto expectedDto = new PostReadDto(1L, "test title1", "test desc1", List.of("test_tag1"), 2L, 3L);

    @Test
    void findOnePost_shouldReturnPostSuccess() {
        Mockito.when(postSearchRepository.findPostById(VALID_ID)).thenReturn(Optional.of(post));
        Mockito.when(postMapper.toPostReadDto(post)).thenReturn(expectedDto);

        PostReadDto actualDto = postService.findOnePost(VALID_ID);

        Assertions.assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void findOnePost_shouldPostThrowEntityNotFoundException() {
        Mockito.when(postSearchRepository.findPostById(INVALID_ID)).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> postService.findOnePost(INVALID_ID))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));
    }


    @Test
    void incrementLikesCount_shouldReturnIncLikesCountSuccess() {
        Long expectedLikes = 100L;
        Mockito.when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        Mockito.when(postManagementRepository.incrementLikesCount(VALID_ID)).thenReturn(expectedLikes);

        Long actualLikes = postService.incrementLikesCount(VALID_ID);

        Assertions.assertThat(actualLikes).isEqualTo(expectedLikes);
    }

    @Test
    void incrementLikesCount_shouldPostThrowEntityNotFoundException() {
        Mockito.when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);

        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> postService.incrementLikesCount(INVALID_ID))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));
    }

    @Test
    void findPageByCriteria() {
    }

    @Test
    void updatePost_shouldReturnUpdatedPostSuccess() {
        PostUpdateDto givenDto = new PostUpdateDto(VALID_ID, "testTitle", "testText", List.of("test_tag1"));
        Mockito.when(postSearchRepository.findPostById(VALID_ID)).thenReturn(Optional.of(post));
        Mockito.doNothing().when(postMapper).updatePost(givenDto, post);
        Mockito.when(postManagementRepository.update(Mockito.any(Post.class))).thenReturn(post);
        Mockito.when(postMapper.toPostReadDto(post)).thenReturn(expectedDto);

        PostReadDto actualDto = postService.updatePost(VALID_ID, givenDto);

        Assertions.assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void updatePost_shouldPostThrowEntityNotFoundExceptionFail() {
        PostUpdateDto givenDto = new PostUpdateDto(INVALID_ID, "testTitle", "testText", List.of("test_tag1"));
        Mockito.when(postSearchRepository.findPostById(INVALID_ID)).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> postService.updatePost(INVALID_ID, givenDto))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

    }


    @Test
    void savePost_shouldReturnPersistentPostSuccess() {
        PostReadDto expectedDto = new PostReadDto(1L, "testTitle1", "test desc1", List.of("test_tag1"), 2L, 3L);
        PostCreateDto givenDto = new PostCreateDto("testTitle", "test text", List.of("test_tag1"));
        Mockito.when(postManagementRepository.existsByTitle(givenDto.title())).thenReturn(false);
        Mockito.when(postMapper.toPost(Mockito.any(PostCreateDto.class))).thenReturn(post);
        Mockito.when(postManagementRepository.save(Mockito.any(Post.class))).thenReturn(post);
        Mockito.when(postMapper.toPostReadDto(post)).thenReturn(expectedDto);

        PostReadDto actualDto = postService.savePost(givenDto);

        Assertions.assertThat(actualDto).isEqualTo(expectedDto);

        Mockito.verify(postManagementRepository, Mockito.times(1)).save(post);
        Mockito.verify(postMapper, Mockito.times(1)).toPostReadDto(post);

    }

    @Test
    void savePost_shouldThrowTitleAlreadyExistsExceptionFail() {
        PostCreateDto givenDto = new PostCreateDto("testTitle", "test text", List.of("test_tag1"));
        Mockito.when(postManagementRepository.existsByTitle(givenDto.title())).thenReturn(true);

        Assertions.assertThatExceptionOfType(TitleAlreadyExistsException.class)
                .isThrownBy(() -> postService.savePost(givenDto))
                .withMessage(messageSource.getMessage(MessageKey.POST_TITLE_EXISTS_EX, new Object[]{givenDto.title()}, Locale.ENGLISH));

        Mockito.verify(postManagementRepository, Mockito.times(0)).save(post);
        Mockito.verify(postMapper, Mockito.times(0)).toPostReadDto(post);
    }

    @Test
    void deletePost_shouldCallDeletePostSuccess() {
        Mockito.when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        Mockito.doNothing().when(postManagementRepository).delete(VALID_ID);
        Mockito.doNothing().when(commentRepository).deleteByPostId(VALID_ID);

        postService.deletePost(VALID_ID);

        Mockito.verify(postManagementRepository, Mockito.times(1)).delete(VALID_ID);
        Mockito.verify(commentRepository, Mockito.times(1)).deleteByPostId(VALID_ID);

    }

    @Test
    void deletePost_shouldThrowEntityNotFoundException() {
        Mockito.when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);


        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> postService.deletePost(INVALID_ID))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

        Mockito.verify(postManagementRepository, Mockito.times(0)).delete(INVALID_ID);
        Mockito.verify(commentRepository, Mockito.times(0)).deleteByPostId(INVALID_ID);


    }

    @Test
    void getImage_shouldReturnArrayBytesSuccess() {
        byte[] expectedImage = new byte[]{1, 2, 3, 4};
        Mockito.when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        Mockito.when(postManagementRepository.getImagePath(VALID_ID)).thenReturn(Optional.of(post.getImagePath()));
        Mockito.when(fileService.getFile(post.getImagePath())).thenReturn(Optional.of(expectedImage));

        byte[] image = postService.getImage(VALID_ID);

        Assertions.assertThat(image).isEqualTo(expectedImage);
    }
    @Test
    void getImage_shouldThrowEntityNotFoundExceptionFail() {
        Mockito.when(postManagementRepository.existsById(INVALID_ID)).thenReturn(false);

        Assertions.assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> postService.getImage(INVALID_ID))
                .withMessage(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{INVALID_ID}, Locale.ENGLISH));

        Mockito.verify(postManagementRepository, Mockito.times(0)).getImagePath(INVALID_ID);
        Mockito.verify(fileService, Mockito.times(0)).getFile(post.getImagePath());
    }

    @Test
    void updateImage_shouldCallSaveFileAndUpdateImagePathSuccess() {
        MultipartFile image =new MockMultipartFile("image",new byte[]{1, 2, 3, 4});
        Mockito.when(postManagementRepository.existsById(VALID_ID)).thenReturn(true);
        Mockito.doNothing().when(fileService).saveFile(Mockito.any(MultipartFile.class),Mockito.anyString());
        Mockito.doNothing().when(postManagementRepository).updateImagePath(Mockito.anyLong(),Mockito.anyString());

        postService.updateImage(VALID_ID,image);

        Mockito.verify(fileService,Mockito.times(1)).saveFile(Mockito.any(MultipartFile.class),Mockito.anyString());
        Mockito.verify(postManagementRepository,Mockito.times(1)).updateImagePath(Mockito.anyLong(),Mockito.anyString());
    }
}