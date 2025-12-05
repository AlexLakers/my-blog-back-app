package com.alex.blog.api.dto;

public record CommentCreateDto(String text, Long postId) {
}
