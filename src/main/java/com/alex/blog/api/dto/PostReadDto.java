package com.alex.blog.api.dto;

import java.util.List;


public record PostReadDto(Long id,
                          String title,
                          String text,
                          List<String> tags,
                          Long likesCount,
                          Long commentsCount) {
}
