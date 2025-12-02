package com.alex.blog.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldNameConstants
public class Comment {
    private Long id;
    private String text;
    private Long postId;
}
