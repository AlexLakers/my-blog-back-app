package com.alex.blog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentCreateDto(
                            @NotBlank(message = "The text should be not blank ")
                            String text,

                            @NotNull(message = "The postId should be not null")
                            Long postId) {
}
