package com.alex.blog.service.impl;

import com.alex.blog.aop.annotation.Loggable;
import com.alex.blog.api.dto.PostCreateDto;
import com.alex.blog.api.dto.PostReadDto;
import com.alex.blog.api.dto.PostUpdateDto;
import com.alex.blog.exception.*;
import com.alex.blog.mapper.PostMapper;
import com.alex.blog.model.Post;
import com.alex.blog.repository.CommentRepository;
import com.alex.blog.repository.PostManagementRepository;
import com.alex.blog.repository.PostSearchRepository;
import com.alex.blog.search.Criteria;
import com.alex.blog.search.PostPageDto;
import com.alex.blog.search.SearchDto;
import com.alex.blog.service.MessageKey;
import com.alex.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {
    private final PostManagementRepository postManagementRepository;
    private final PostSearchRepository postSearchRepository;
    private final PostMapper postMapper;
    private final CommentRepository commentRepository;
    private final MessageSource messageSource;
    private final Integer MAX_LENGTH_TXT = 128;


    @Override
    @Loggable
    public PostReadDto findOnePost(Long postId) {
        return postSearchRepository.findPostById(postId)
                .map(postMapper::toPostReadDto)
                .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{postId}, Locale.ENGLISH)));

    }

    @Transactional
    @Override
    @Loggable
    public Long incrementLikesCount(Long postId) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{postId}, Locale.ENGLISH));
        }
        return postManagementRepository.incrementLikesCount(postId);
    }

    @Override
    @Loggable
    public PostPageDto findPageByCriteria(SearchDto searchDto) {
        Map<Boolean, List<String>> tokens = Arrays.stream(searchDto.search().split(" "))
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.partitioningBy(token -> token.startsWith("#")));


        List<String> tags = tokens.get(Boolean.TRUE).stream()
                .map(t -> t.replace("#", ""))
                .collect(Collectors.toList());

        String title = tokens.get(Boolean.FALSE).stream()
                .collect(Collectors.joining(" "));


        Pageable pageable = PageRequest.of(searchDto.pageNumber() - 1, searchDto.pageSize());
        Criteria criteria = new Criteria(title, tags);
        Page<Post> page = postSearchRepository.findPostsByCriteriaAndPageable(criteria, pageable);


        page.stream()
                .filter(p -> p.getText().length() > 128)
                .forEach(post -> {
                    String originText = post.getText();
                    post.setText(truncateText(originText, MAX_LENGTH_TXT));
                });


        return buildPostPageDto(page);
    }

    private String truncateText(String text, Integer length) {
        return text.substring(0, length).concat("...");
    }

    @Override
    @Transactional
    @Loggable
    public PostReadDto updatePost(Long postId, PostUpdateDto postUpdateDto) {

        Post copiedPost = postSearchRepository.findPostById(postId)
                .map(post -> {
                    postMapper.updatePost(postUpdateDto, post);
                    return post;
                }).orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{postId}, Locale.ENGLISH)));

        return postMapper.toPostReadDto(postManagementRepository.update(copiedPost));

    }

    @Transactional
    public void deletePost(Long postId) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{postId}, Locale.ENGLISH));
        }
        postManagementRepository.delete(postId);
        commentRepository.deleteByPostId(postId);
    }

    @Override
    public byte[] getImage(Long postId) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{postId}, Locale.ENGLISH));
        }

        return postManagementRepository.getImage(postId)
                .orElseThrow(() -> new ImageNotFoundException(messageSource.getMessage(MessageKey.IMAGE_NOT_FOUND_EX, new Object[]{postId}, Locale.ENGLISH)));
    }


    @SneakyThrows
    @Override
    @Transactional
    public boolean updateImage(long postId, MultipartFile image) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException(messageSource.getMessage(MessageKey.POST_NOT_FOUND, new Object[]{postId}, Locale.ENGLISH));
        }

        return postManagementRepository.updateImage(postId, image.getBytes());
    }

    @Transactional
    @Override
    public PostReadDto savePost(PostCreateDto postCreateDto) {
        if (postManagementRepository.existsByTitle(postCreateDto.title())) {
            throw new TitleAlreadyExistsException(messageSource.getMessage(MessageKey.POST_TITLE_EXISTS_EX, new Object[]{postCreateDto.title()}, Locale.ENGLISH));
        }


        return Optional.ofNullable(postCreateDto)
                .map(postMapper::toPost)
                .map(postManagementRepository::save)
                .map(postMapper::toPostReadDto)
                .orElseThrow(() -> new EntityCreationException(messageSource.getMessage(MessageKey.POST_CREATION_EX, null, Locale.ENGLISH)));
    }

    private PostPageDto buildPostPageDto(Page<Post> page) {
        List<PostReadDto> content = page.getContent().stream().map(postMapper::toPostReadDto).toList();
        return new PostPageDto(content, page.hasPrevious(), page.hasNext(), page.getTotalPages() - 1);
    }


}
