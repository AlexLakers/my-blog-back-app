package com.alex.blog.service.impl;

import com.alex.blog.api.dto.PostCreateDto;
import com.alex.blog.api.dto.PostReadDto;
import com.alex.blog.api.dto.PostUpdateDto;
import com.alex.blog.exception.EntityCreationException;
import com.alex.blog.exception.EntityNotFoundException;
import com.alex.blog.exception.TitleAlreadyExistsException;
import com.alex.blog.mapper.PostMapper;
import com.alex.blog.model.Post;
import com.alex.blog.repository.CommentRepository;
import com.alex.blog.repository.PostManagementRepository;
import com.alex.blog.repository.PostSearchRepository;
import com.alex.blog.search.Criteria;
import com.alex.blog.search.PostPageDto;
import com.alex.blog.search.SearchDto;
import com.alex.blog.service.FileService;
import com.alex.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostManagementRepository postManagementRepository;
    private final PostSearchRepository postSearchRepository;
    private final PostMapper postMapper;
    private final CommentRepository commentRepository;
    private final FileService fileService;
    private final Integer MAX_LENGTH_TXT = 128;


    @Override
    public PostReadDto findOnePost(Long postId) {
        return postSearchRepository.findPostById(postId)
                .map(postMapper::toPostReadDto)
                .orElseThrow(() -> new EntityNotFoundException("The post not found by id:%d".formatted(postId)));

    }

    @Override
    public Long incrementLikesCount(Long postId) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException("The post not found by id:%d"
                    .formatted(postId));
        }
        return postManagementRepository.incrementLikesCount(postId);
    }

    @Override
    public PostPageDto findPageByCriteria(SearchDto searchDto) {
        Map<Boolean, List<String>> tokens = Arrays.stream(searchDto.search().split(" "))
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.partitioningBy(token -> token.startsWith("#")));


        List<String> tags = tokens.get(Boolean.TRUE).stream()
                .map(t -> t.replace("#", ""))
                .collect(Collectors.toList());

        String title = tokens.get(Boolean.FALSE).stream()
                .collect(Collectors.joining(" "));


        Pageable pageable = PageRequest.of(searchDto.pageNumber()-1, searchDto.pageSize());
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
    public PostReadDto updatePost(Long postId, PostUpdateDto postUpdateDto) {

        return postSearchRepository.findPostById(postId)
                .map(post -> {
                            postMapper.updatePost(postUpdateDto, post);
                            return postManagementRepository.update(post);
                        }
                )
                .map(postMapper::toPostReadDto)
                .orElseThrow(() -> new EntityNotFoundException("The post with id:%1$d not found"
                        .formatted(postId)));
    }

    public void deletePost(Long postId) {
        if (!postManagementRepository.existsById(postId)) {
            throw new EntityNotFoundException("The post with id:%1$d not found"
                    .formatted(postId));
        }
        postManagementRepository.delete(postId);
        commentRepository.deleteByPostId(postId);

    }

    @Override
    public byte[] getImage(Long postId) {
        String imagePath = postManagementRepository.getImagePath(postId);
        return fileService.getFile(imagePath).orElseThrow();
    }

    @SneakyThrows
    @Override
    public void updateImage(long postId, MultipartFile file) {
        InputStream is = file.getInputStream();
        String imageName = postId + "/" + UUID.randomUUID()+(Objects.requireNonNull(file.getOriginalFilename()));

        postManagementRepository.updateImagePath(postId, imageName);
        fileService.saveFile(is, imageName);
    }

    public PostReadDto savePost(PostCreateDto postCreateDto) {
        if (postManagementRepository.existsByTitle(postCreateDto.title())) {
            throw new TitleAlreadyExistsException("The title: %s already exists".formatted(postCreateDto.title()));
        }

        return Optional.ofNullable(postCreateDto)
                .map(postMapper::toPost)
                .map(postManagementRepository::save)
                .map(postMapper::toPostReadDto)
                .orElseThrow(() -> new EntityCreationException("An error occurred during saving a new post"));

    }

    private PostPageDto buildPostPageDto(Page<Post> page) {
        List<PostReadDto> content = page.getContent().stream().map(postMapper::toPostReadDto).toList();
        return new PostPageDto(content, page.hasPrevious(), page.hasNext(), page.getTotalPages() - 1);
    }


}
