package com.alex.blog.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;

public interface FileService {
    void saveFile( MultipartFile file,/*InputStream inputStream,*/  String fileName);
    Optional<byte[]> getFile(String fileName);
    void deleteFile(String fileName);
}
