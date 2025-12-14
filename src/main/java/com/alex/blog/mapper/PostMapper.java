package com.alex.blog.mapper;

import com.alex.blog.api.dto.PostCreateDto;
import com.alex.blog.api.dto.PostReadDto;
import com.alex.blog.api.dto.PostUpdateDto;
import com.alex.blog.model.Post;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import static org.mapstruct.NullValuePropertyMappingStrategy.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@Component
public interface PostMapper {
    @Mapping(target = "likesCount", constant = "0L")
    @Mapping(target = "commentsCount", constant = "0L")
    Post toPost(PostCreateDto postCreateDto);
    @BeanMapping( nullValuePropertyMappingStrategy = IGNORE)
    void updatePost(PostUpdateDto postUpdateDto, @MappingTarget Post post);
    PostReadDto toPostReadDto(Post post);

}
