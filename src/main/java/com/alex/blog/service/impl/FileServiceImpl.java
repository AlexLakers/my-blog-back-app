package com.alex.blog.service.impl;

import com.alex.blog.service.FileService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
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


    @Override
    @SneakyThrows
    @Transactional
    public void saveFile(MultipartFile file, String fileName) {

        Path fullPath = Path.of(baseDir, fileName);

        Files.createDirectories(fullPath.getParent());
        file.transferTo(fullPath.toFile());


    }


    @SneakyThrows
    @Override
    public Optional<byte[]> getFile(String fileName) {

        return buildAndCheckPath(fileName)
                .flatMap(this::readAllBytesSafe);

    }

    private Optional<Path> buildAndCheckPath(String fileName) {
        return Optional.ofNullable(fileName)
                .filter(file -> !file.isEmpty())
                .map(file -> Path.of(baseDir, file))
                .filter(Files::exists)
                .filter(Files::isRegularFile);
    }

    private Optional<byte[]> readAllBytesSafe(Path path) {
        try {
            return Optional.of(Files.readAllBytes(path));
        } catch (IOException e) {
            return Optional.empty();
        }
    }


}
