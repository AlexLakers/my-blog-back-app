package com.alex.blog.model;


import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldNameConstants
public class Post {
    private Long id;
    private String title;
    private String text;
    private List<String> tags;
    private String imagePath;
    private Long likesCount;
    private Long commentsCount;
}




