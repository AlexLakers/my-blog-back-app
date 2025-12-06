package com.alex.blog.api.dto;

public record CommentReadDto(Long id, String text, Long postId) {
}
