package com.alex.blog.api.rest.controller;

import com.alex.blog.api.dto.CommentCreateDto;
import com.alex.blog.api.dto.CommentReadDto;
import com.alex.blog.api.dto.CommentUpdateDto;
import com.alex.blog.model.Comment;
import com.alex.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentRestController {
    private final CommentService commentService;

    @GetMapping(path = "/{postId}/comments/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommentReadDto> getComment(@PathVariable("postId") Long postId,
                                                     @PathVariable("commentId") Long commentId) {

        return ResponseEntity.ok(commentService.findOneComment(postId, commentId));
    }

    @GetMapping(path = "{postId}/comments",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CommentReadDto>> getComments(@PathVariable("postId") Long postId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(commentService.findCommentsByPostId(postId));
    }

    @PutMapping(path = "/{postId}/comments/{commentId}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommentReadDto> updateComment(@PathVariable("postId") Long postId,
                                                        @PathVariable("commentId") Long commentId,
                                                        @RequestBody CommentUpdateDto commentUpdateDto) {

        return Objects.equals(postId, commentUpdateDto.postId()) && Objects.equals(commentId, commentUpdateDto.id())
                ? ResponseEntity
                .status(HttpStatus.OK)

                .body(commentService.updateComment(postId, commentId, commentUpdateDto))
                : ResponseEntity
                .status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping(path = "/{postId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommentReadDto> saveComment(@PathVariable("postId") Long postId,
                                                      @RequestBody CommentCreateDto commentCreateDto) {
        return Objects.equals(postId, commentCreateDto.postId())
                ? ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.saveComment(postId, commentCreateDto))

                : ResponseEntity
                .status(HttpStatus.BAD_REQUEST).build();
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("postId") Long postId,
                       @PathVariable("commentId") Long commentId) {
        commentService.deleteComment(postId, commentId);
    }


}
