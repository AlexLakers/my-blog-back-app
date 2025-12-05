package com.alex.blog.api.dto;

import java.util.List;

public record PostCreateDto(String title,
                            String text,
                            List<String> tags) {
}
