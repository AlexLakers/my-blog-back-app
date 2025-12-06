package com.alex.blog.mapper;

import com.alex.blog.api.dto.CommentCreateDto;
import com.alex.blog.api.dto.CommentReadDto;
import com.alex.blog.api.dto.CommentUpdateDto;
import com.alex.blog.model.Comment;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.*;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentReadDto toCommentReadDto(Comment comment);
    Comment toComment(CommentCreateDto commentCreateDto);
    List<CommentReadDto> toCommentReadDtoList (List<Comment> comments);
    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    Comment updateComment (CommentUpdateDto commentUpdateDto,@MappingTarget Comment comment);
}
