package com.alex.blog.exception.handler;

import com.alex.blog.exception.EntityNotFoundException;
import com.alex.blog.exception.EntityCreationException;
import com.alex.blog.exception.TitleAlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice(basePackages = "com.alex.blog.api.rest.controller.1")
public class GlobalRestControllerAdvice {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleCommentNotFoundException(EntityNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

       @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleConstraintViolationException(MethodArgumentNotValidException ex) {
        return status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }


    @ExceptionHandler(EntityCreationException.class)
    public ResponseEntity<String> handleEntityCreationException(EntityCreationException ex) {
        return status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }
    @ExceptionHandler(TitleAlreadyExistsException.class)
    public ResponseEntity<String> handleTitleAlreadyExistsException(TitleAlreadyExistsException ex) {
        return status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body( ex.getMessage());
    }
}
