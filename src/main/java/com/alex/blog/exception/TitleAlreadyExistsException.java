package com.alex.blog.exception;

public class TitleAlreadyExistsException extends RuntimeException {
    public TitleAlreadyExistsException(String message) {
        super(message);
    }
    public TitleAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
