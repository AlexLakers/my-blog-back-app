package com.alex.blog.search;

import com.alex.blog.api.dto.PostReadDto;

import java.util.List;

public record PostPageDto(
         List<PostReadDto> posts,
         boolean hasPrev,
         boolean hasNext,
         Integer lastPage
) {
}
