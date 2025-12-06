package com.alex.blog.exception;

public class ImageNotFoundException extends RuntimeException {

    public ImageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public ImageNotFoundException(String message) {
        super(message);
    }
}
