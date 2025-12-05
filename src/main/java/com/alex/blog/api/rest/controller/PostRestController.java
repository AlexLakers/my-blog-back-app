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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.invoke.VarHandle;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostRestController {

    public final PostService postService;


    @PutMapping(value = "/{postId}/image",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
                .contentType( MediaType.APPLICATION_OCTET_STREAM)
                .body(image);
    }

    @PatchMapping(path = "/{postId}/likes")
    public ResponseEntity<Long> incrementLikesCount(@PathVariable("postId") Long postId) {
       return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.incrementLikesCount(postId));
    }
}

