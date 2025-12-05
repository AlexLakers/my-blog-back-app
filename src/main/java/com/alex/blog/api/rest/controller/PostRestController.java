package com.alex.blog.api.rest.controller;

import com.alex.blog.api.dto.PostCreateDto;
import com.alex.blog.api.dto.PostReadDto;
import com.alex.blog.api.dto.PostUpdateDto;
import com.alex.blog.search.PostPageDto;
import com.alex.blog.search.SearchDto;
import com.alex.blog.service.PostService;
import jdk.jfr.ContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.VarHandle;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostRestController {

    public final PostService postService;

    @PutMapping(path = "/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostReadDto> update(@PathVariable("postId") Long postId,
                                              @RequestBody PostUpdateDto postUpdateDto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.updatePost(postId, postUpdateDto));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostPageDto> search(@Validated SearchDto searchDto
                                                /*@RequestParam("search") String search,
                                              @RequestParam("pageNumber") int pageNumber,
                                              @RequestParam("pageSize") int pageSize*/) {


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.findPageByCriteria(searchDto/*new SearchDto(search, pageNumber, pageSize)*/));
    }

    @PutMapping(value = "/{postId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateImage(@PathVariable("postId") Long postId,
                                            @RequestParam("image") MultipartFile image) {
        postService.updateImage(postId, image);
        return ResponseEntity.ok().build();

    }

    @GetMapping(value = "/{postId}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable("postId") Long postId) {
        System.out.println("dsadsadasas");
        byte[] image = postService.getImage(postId);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(image);
    }

    @PostMapping(path = "/{postId}/likes")
    public ResponseEntity<Long> incrementLikesCount(@PathVariable("postId") Long postId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.incrementLikesCount(postId));
    }

    @DeleteMapping(path = "/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public void deletePostWithComments(@PathVariable("postId") Long postId) {
        postService.deletePost(postId);
    }
}

