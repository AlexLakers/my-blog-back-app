package com.alex.blog.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SearchDto(
                        @NotBlank(message = "The search parameter should be not empty")
                        String search,

                        @PositiveOrZero(message = "Page number should be positive value or zero")
                        @NotNull(message = "Page pageNumber should be not null value")
                        Integer pageNumber,

                        @NotNull(message = "Page size should be not null value")
                        @Positive(message = "Page size should be positive value")
                        Integer pageSize) {
}
