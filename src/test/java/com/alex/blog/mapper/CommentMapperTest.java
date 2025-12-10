package com.alex.blog.mapper;

import com.alex.blog.api.dto.CommentCreateDto;
import com.alex.blog.api.dto.CommentReadDto;
import com.alex.blog.api.dto.CommentUpdateDto;
import com.alex.blog.config.TestMapperConfig;
import com.alex.blog.model.Comment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
@ContextConfiguration(classes = TestMapperConfig.class)
@ExtendWith(SpringExtension.class)
class CommentMapperTest {

    @Autowired
    private CommentMapper commentMapper;
    private Comment comment;

    @BeforeEach
    void setUp() {
        comment=new Comment(null, "test comment", 1L);
    }

    @Test
    void toCommentReadDto_shouldReturnCommentReadDto() {
        CommentReadDto expectedComment=new CommentReadDto(null,"test comment",1L);

        CommentReadDto actual=commentMapper.toCommentReadDto(comment);

        Assertions.assertThat(actual).isEqualTo(expectedComment);
    }

    @Test
    void toComment_shouldReturnComment() {
        CommentCreateDto givenDto=new CommentCreateDto("test comment",1L);

        Comment actual=commentMapper.toComment(givenDto);

        Assertions.assertThat(actual).isEqualTo(comment);
    }

    @Test
    void updateComment_shouldSetFieldsComment() {
        CommentUpdateDto givenDto=new CommentUpdateDto(null,"UPDATED",1L);

        commentMapper.updateComment(givenDto,comment);

        Assertions.assertThat(comment)
                .hasFieldOrPropertyWithValue(Comment.Fields.text,givenDto.text())
                .hasFieldOrPropertyWithValue(Comment.Fields.id,givenDto.id());
    }
}