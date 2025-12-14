package com.alex.blog.config;

import com.alex.blog.mapper.CommentMapper;
import com.alex.blog.mapper.PostMapper;
import com.alex.blog.repository.CommentRepository;
import com.alex.blog.repository.PostManagementRepository;
import com.alex.blog.repository.PostSearchRepository;
import com.alex.blog.service.CommentService;
import com.alex.blog.service.PostService;
import com.alex.blog.service.impl.CommentServiceImpl;
import com.alex.blog.service.impl.PostServiceImpl;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

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
    public CommentMapper commentMapper() {
        return Mockito.mock(CommentMapper.class);
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages-test");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public PostService postService() {
        return new PostServiceImpl(
                postManagementRepository(),
                postSearchRepository(),
                postMapper(),
                commentRepository(),
                messageSource());
    }

    @Bean
    public CommentService commentService() {
        return new CommentServiceImpl(
                commentRepository(),
                commentMapper(),
                postManagementRepository(),
                messageSource());
    }

}
