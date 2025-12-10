package com.alex.blog.config;

import com.alex.blog.mapper.PostMapper;
import com.alex.blog.repository.CommentRepository;
import com.alex.blog.repository.PostManagementRepository;
import com.alex.blog.repository.PostSearchRepository;
import com.alex.blog.service.FileService;
import com.alex.blog.service.PostService;
import com.alex.blog.service.impl.PostServiceImpl;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestServiceConfig {


    @Bean
    public PostManagementRepository postManagementRepository() {
        return Mockito.mock(PostManagementRepository.class);
    }

    @Bean
    public PostSearchRepository postSearchRepository() {
        return Mockito.mock(PostSearchRepository.class);
    }

    @Bean
    public PostMapper postMapper() {
        return Mockito.mock(PostMapper.class);
    }

    @Bean
    public CommentRepository commentRepository() {
        return Mockito.mock(CommentRepository.class);
    }

    @Bean
    public FileService fileService() {
        return Mockito.mock(FileService.class);
    }

    @Bean
    public PostService postService() {
        return new PostServiceImpl(
                postManagementRepository(),
                postSearchRepository(),
                postMapper(),
                commentRepository(),
                fileService());
    }

}
