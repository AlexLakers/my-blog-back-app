package com.alex.blog.service;

import java.io.InputStream;
import java.util.Optional;

public interface FileService {
    void saveFile(InputStream content, String folder, String fileName);
    boolean deleteFile(String folder,String fileName);
    Optional<byte[]> getFile(String folder, String fileName);
}
