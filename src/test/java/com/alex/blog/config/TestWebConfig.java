package com.alex.blog.config;

import com.alex.blog.api.rest.controller.CommentRestController;
import com.alex.blog.api.rest.controller.PostRestController;
import com.alex.blog.config.RestConfiguration;
import com.alex.blog.service.CommentService;
import com.alex.blog.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@Import({RestConfiguration.class})
@EnableWebMvc
@ComponentScan(basePackages = {"com.alex.blog.api.rest.controller",
        "com.alex.blog.exception.handler",
        "com.alex.blog.service",
        "com.alex.blog.mapper"})
@PropertySource("classpath:application-test.properties")
public class TestWebConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }
    @Bean
    public HttpMessageConverter<Object> httpMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }


}
