package com.alex.blog.config;

import com.alex.blog.mapper.CommentMapper;
import com.alex.blog.mapper.CommentMapperImpl;
import com.alex.blog.mapper.PostMapper;
import com.alex.blog.mapper.PostMapperImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestMapperConfig {
    @Bean
    public PostMapper postMapper() {
        return new PostMapperImpl();
    }
    @Bean
    public CommentMapper commentMapper(){
        return new CommentMapperImpl();
    }
}
