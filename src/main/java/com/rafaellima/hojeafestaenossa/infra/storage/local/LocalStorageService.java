package com.rafaellima.hojeafestaenossa.infra.storage.local;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.infra.storage.StorageService;

import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    @Value("${storage.local.base-path:./uploads}")
    private String basePath;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(basePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public String upload(String objectName, InputStream inputStream, long contentLength, String contentType) {
        try {
            String extension = getExtension(contentType);
            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(basePath, fileName);

            try (OutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                inputStream.transferTo(outputStream);
            }

            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not upload file", e);
        }
    }

    @Override
    public void delete(String objectName) {
        try {
            String fileName = objectName.substring("/uploads/".length());
            Path filePath = Paths.get(basePath, fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file", e);
        }
    }

    private String getExtension(String contentType) {
        if (contentType == null) return "";
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "video/mp4" -> ".mp4";
            case "video/quicktime" -> ".mov";
            default -> "";
        };
    }

}
