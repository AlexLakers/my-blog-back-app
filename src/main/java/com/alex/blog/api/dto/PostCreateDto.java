package com.alex.blog.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PostCreateDto( @NotBlank(message = "The title should be not blank ")
                            String title,

                             @NotBlank(message = "The text should be not blank ")
                            String text,

                             @NotBlank(message = "The text should be not blank ")
                            List<String> tags) {
}
