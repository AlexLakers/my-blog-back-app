package com.alex.blog.api.dto;

import java.util.List;

public record PostUpdateDto(Long id,
                            String title,
                            String text,
                            List<String> tags) {
}
