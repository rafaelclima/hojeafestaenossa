package com.rafaellima.hojeafestaenossa.upload.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.infra.storage.StorageService;
import com.rafaellima.hojeafestaenossa.upload.domain.MediaType;
import com.rafaellima.hojeafestaenossa.upload.domain.Upload;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadMediaService {

    private final UploadRepository uploadRepository;
    private final StorageService storageService;
    private final ImageCompressionService imageCompressionService;
    private final ThumbnailGenerationService thumbnailGenerationService;

    @Async("taskExecutor")
    public void execute(File originalFile, String originalFileName, String contentType, long fileSize, Event event,
            String message) {
        File fileToUpload = null;
        File compressedFile = null;
        File thumbnailFile = null;

        try {
            MediaType mediaType = resolveMediaType(contentType);

            if (mediaType == MediaType.PHOTO) {
                log.info("Starting image compression for file: {}", originalFileName);
                compressedFile = imageCompressionService.execute(originalFile);
                fileToUpload = compressedFile;
                log.info("Image compression finished. Original size: {}, Compressed size: {}", fileSize,
                        fileToUpload.length());
            } else {
                fileToUpload = originalFile;
            }

            String storageKey = generateStorageKey(event.getAccessToken(), originalFileName, mediaType);
            long finalFileSize = fileToUpload.length();
            String finalContentType = (mediaType == MediaType.PHOTO) ? "image/jpeg" : contentType;

            // Processamento de Thumbnail (apenas para fotos por enquanto)
            String thumbnailUrl = null;
            if (mediaType == MediaType.PHOTO) {
                thumbnailFile = thumbnailGenerationService.execute(fileToUpload);
                String thumbnailKey = storageKey.replace(".jpg", "_thumb.jpg");
                try (InputStream thumbStream = new FileInputStream(thumbnailFile)) {
                    thumbnailUrl = storageService.upload(
                            thumbnailKey,
                            thumbStream,
                            thumbnailFile.length(),
                            "image/jpeg");
                }
            }

            String publicUrl;
            try (InputStream inputStream = new FileInputStream(fileToUpload)) {
                publicUrl = storageService.upload(
                        storageKey,
                        inputStream,
                        finalFileSize,
                        finalContentType);
            }

            Upload upload = new Upload(
                    event.getId(),
                    mediaType,
                    storageKey,
                    originalFileName,
                    finalFileSize,
                    message,
                    publicUrl,
                    thumbnailUrl);

            uploadRepository.save(upload);
            log.info("Upload record saved for file: {}", originalFileName);

        } catch (IOException e) {
            log.error("Error processing upload for file: {}", originalFileName, e);
        } finally {
            if (originalFile != null && originalFile.exists()) {
                if (!originalFile.delete()) {
                    log.warn("Could not delete temporary original file: {}", originalFile.getAbsolutePath());
                }
            }
            if (compressedFile != null && compressedFile.exists()) {
                if (!compressedFile.delete()) {
                    log.warn("Could not delete temporary compressed file: {}", compressedFile.getAbsolutePath());
                }
            }
            if (thumbnailFile != null && thumbnailFile.exists()) {
                if (!thumbnailFile.delete()) {
                    log.warn("Could not delete temporary thumbnail file: {}", thumbnailFile.getAbsolutePath());
                }
            }
        }
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType != null && contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        }
        return MediaType.PHOTO;
    }

    private String generateStorageKey(String eventToken, String originalFileName, MediaType mediaType) {
        String extension = getExtension(originalFileName);
        if (mediaType == MediaType.PHOTO) {
            extension = "jpg";
        }
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
