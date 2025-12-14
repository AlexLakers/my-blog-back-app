package com.alex.blog.service;


import com.alex.blog.api.dto.PostCreateDto;
import com.alex.blog.api.dto.PostReadDto;
import com.alex.blog.api.dto.PostUpdateDto;
import com.alex.blog.search.PostPageDto;
import com.alex.blog.search.SearchDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;


public interface PostService {
    PostReadDto findOnePost(Long id);
    Long incrementLikesCount(Long id);
    PostPageDto findPageByCriteria(SearchDto searchDto);
    PostReadDto updatePost(Long postId,PostUpdateDto postUpdateDto);
    PostReadDto savePost(PostCreateDto postCreateDto);
    void deletePost(Long postId);
    byte[] getImage(Long postId);
    boolean updateImage(long postId, MultipartFile file);
}
