package com.alex.blog.exception;

import java.io.Serializable;

public class ImageSavingException extends RuntimeException{

    public ImageSavingException(String message) {
        super(message);
    }
    public ImageSavingException(String message, Throwable cause) {
        super(message, cause);
    }

}
