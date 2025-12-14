package com.alex.blog.search;

import java.util.List;

public record Criteria(String title,
                       List<String> tags
) {
}
