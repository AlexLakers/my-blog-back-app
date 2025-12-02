package com.alex.blog.search;

import com.alex.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostSearchRepository {
     Page<Post> findByTitleContainingAndTag(String title, String tag, Pageable pageable);
}
