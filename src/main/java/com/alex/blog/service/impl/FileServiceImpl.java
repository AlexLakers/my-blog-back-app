package com.alex.blog.service.impl;

import com.alex.blog.service.FileService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
public class FileServiceImpl implements FileService {

   @Value("${blog.image.base.dir:/home/my-blog/}")
    private String baseDir;


    @SneakyThrows
    @Override
    public void saveFile(InputStream content, String fileName) {
        Path fullPath = Path.of(baseDir, fileName);
        Files.createDirectories(fullPath.getParent());
        Files.copy(content, fullPath, StandardCopyOption.REPLACE_EXISTING);
    }



    @SneakyThrows
    @Override
    public Optional<byte[]> getFile(String fileName) {
        Path fullPath = Path.of(baseDir, fileName);
        return Optional.of(fullPath)
                .filter(Files::exists)
                .filter(Files::isRegularFile)
                .flatMap(this::readAllBytesSafe);

    }


    private Optional<byte[]> readAllBytesSafe(Path path) {
        try {
            return Optional.of(Files.readAllBytes(path));
        } catch (IOException e) {
            return Optional.empty();
        }
    }


}
