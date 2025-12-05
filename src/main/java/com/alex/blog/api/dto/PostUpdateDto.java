package com.alex.blog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostUpdateDto(Long id,

                            @NotBlank(message = "The title should be not blank ")
                            String title,

                            @NotBlank(message = "The text should be not blank ")
                            String text,

                            @NotNull(message = "The tags should be not null")
                            List<String> tags) {
}
