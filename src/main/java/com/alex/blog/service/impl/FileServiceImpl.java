package com.alex.blog.service.impl;

import com.alex.blog.service.FileService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class FileServiceImpl implements FileService {
    @Value("${blog.image.base.dir:/blog}")
    private String baseDir;


    @SneakyThrows
    @Override
    public void saveFile(InputStream content, String folder, String fileName) {
        Path fullPath = buildPath(folder, fileName);
        Files.createDirectories(fullPath.getParent());
        Files.createDirectories(fullPath.getParent());
        Files.copy(content, fullPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @SneakyThrows
    @Override
    public boolean deleteFile(String folder, String fileName) {
        Files.deleteIfExists(buildPath(folder, fileName));
        return Files.exists(buildPath(folder, fileName));
    }

    @Override
    public Optional<byte[]> getFile(String folder, String fileName) {
        return Optional.empty();
    }

    private Path buildPath(String folder, String fileName) {
        return Path.of(baseDir, folder, fileName);
    }

}
