package com.rafaellima.hojeafestaenossa.upload.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.infra.storage.StorageService;
import com.rafaellima.hojeafestaenossa.upload.domain.MediaType;
import com.rafaellima.hojeafestaenossa.upload.domain.Upload;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UploadMediaService {

    private final UploadRepository uploadRepository;
    private final StorageService storageService;

    @Transactional
    public Upload execute(MultipartFile file, Event event, String message) throws IOException {
        validateFile(file);

        MediaType mediaType = resolveMediaType(file.getContentType());
        String storageKey = generateStorageKey(event.getAccessToken(), file);
        String originalName = file.getOriginalFilename();
        long fileSize = file.getSize();

        String publicUrl;
        try (InputStream inputStream = file.getInputStream()) {
            publicUrl = storageService.upload(
                    storageKey,
                    inputStream,
                    fileSize,
                    file.getContentType());
        }

        Upload upload = new Upload(
                event.getId(),
                mediaType,
                storageKey,
                originalName,
                fileSize,
                message,
                publicUrl);

        return uploadRepository.save(upload);
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("Tipo de arquivo inválido");
        }

        boolean isImage = contentType.startsWith("image/");
        boolean isVideo = contentType.startsWith("video/");

        if (!isImage && !isVideo) {
            throw new IllegalArgumentException("Tipo de arquivo não suportado. Envie apenas fotos ou vídeos.");
        }
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType != null && contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        }
        return MediaType.PHOTO;
    }

    private String generateStorageKey(String eventToken, MultipartFile file) {
        String extension = getExtension(file.getOriginalFilename());
        return "%s/%s.%s".formatted(
                eventToken,
                UUID.randomUUID(),
                extension);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "bin";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
