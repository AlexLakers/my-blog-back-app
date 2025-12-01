package com.alex.blog.repository;

import java.util.List;

public record Criteria(String title,
                       List<String> tags,
                       Integer pageSize,
                       Integer pageNumber,
                       boolean hasTags,
                       boolean hasTitle) {
}
