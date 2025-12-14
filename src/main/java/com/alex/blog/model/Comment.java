package com.alex.blog.model;

import lombok.*;
import lombok.experimental.FieldNameConstants;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldNameConstants
@EqualsAndHashCode
public class Comment {
    private Long id;
    private String text;
    private Long postId;
}
