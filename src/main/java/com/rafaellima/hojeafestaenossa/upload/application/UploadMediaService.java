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
import com.rafaellima.hojeafestaenossa.shared.util.SlugUtils;
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
            String message, File thumbnailFileFromClient) {
        log.info(">>> INICIANDO PROCESSAMENTO ASSÍNCRONO DO UPLOAD: {} ({} bytes)", originalFileName, fileSize);

        File fileToUpload = null;
        File compressedFile = null;
        File thumbnailFile = null;

        try {
            MediaType mediaType = resolveMediaType(contentType);
            log.info("Media type detectado: {}", mediaType);

            if (mediaType == MediaType.PHOTO) {
                log.info("Starting image compression for file: {}", originalFileName);
                compressedFile = imageCompressionService.execute(originalFile);
                fileToUpload = compressedFile;
                log.info("Image compression finished. Original size: {}, Compressed size: {}", fileSize,
                        fileToUpload.length());
            } else {
                log.info("Vídeo detectado, enviando arquivo original");
                fileToUpload = originalFile;
            }

            String storageKey = generateStorageKey(event.getName(), event.getAccessToken(), originalFileName, mediaType);
            long finalFileSize = fileToUpload.length();
            String finalContentType = (mediaType == MediaType.PHOTO) ? "image/jpeg" : contentType;
            log.info("Storage key gerada: {}", storageKey);

            // Processamento de Thumbnail
            String thumbnailUrl = null;
            
            // Para VÍDEO: usar thumbnail enviada pelo cliente
            if (mediaType == MediaType.VIDEO && thumbnailFileFromClient != null && thumbnailFileFromClient.exists()) {
                log.info("Usando thumbnail enviada pelo cliente para vídeo");
                String thumbnailKey = storageKey.replace(".mp4", "_thumb.jpg").replace(".mov", "_thumb.jpg");
                try (InputStream thumbStream = new FileInputStream(thumbnailFileFromClient)) {
                    thumbnailUrl = storageService.upload(
                            thumbnailKey,
                            thumbStream,
                            thumbnailFileFromClient.length(),
                            "image/jpeg");
                    log.info("Thumbnail de vídeo upload concluído: {}", thumbnailUrl);
                }
            }
            // Para FOTO: gerar thumbnail no backend
            else if (mediaType == MediaType.PHOTO) {
                log.info("Gerando thumbnail para foto...");
                thumbnailFile = thumbnailGenerationService.execute(fileToUpload);
                String thumbnailKey = storageKey.replace(".jpg", "_thumb.jpg");
                log.info("Thumbnail key: {}", thumbnailKey);
                try (InputStream thumbStream = new FileInputStream(thumbnailFile)) {
                    thumbnailUrl = storageService.upload(
                            thumbnailKey,
                            thumbStream,
                            thumbnailFile.length(),
                            "image/jpeg");
                    log.info("Thumbnail de foto upload concluído: {}", thumbnailUrl);
                }
            } else if (mediaType == MediaType.VIDEO) {
                log.warn("Vídeo sem thumbnail enviada pelo cliente. Upload seguirá sem thumbnail.");
            }

            String publicUrl;
            log.info("Iniciando upload do arquivo principal...");
            try (InputStream inputStream = new FileInputStream(fileToUpload)) {
                publicUrl = storageService.upload(
                        storageKey,
                        inputStream,
                        finalFileSize,
                        finalContentType);
            }
            log.info("Upload principal concluído: {}", publicUrl);

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
            log.info(">>> UPLOAD CONCLUÍDO COM SUCESSO! Registro salvo no banco: {}", upload.getId());

        } catch (IOException e) {
            log.error(">>> ERRO CRÍTICO NO PROCESSAMENTO DO UPLOAD: {}", originalFileName, e);
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
            if (thumbnailFileFromClient != null && thumbnailFileFromClient.exists()) {
                if (!thumbnailFileFromClient.delete()) {
                    log.warn("Could not delete temporary thumbnail file from client: {}", thumbnailFileFromClient.getAbsolutePath());
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

    private String generateStorageKey(String eventName, String eventToken, String originalFileName, MediaType mediaType) {
        String extension = getExtension(originalFileName);
        if (mediaType == MediaType.PHOTO) {
            extension = "jpg";
        }
        String slug = SlugUtils.slugify(eventName);
        String prefix = slug.isEmpty() ? eventToken : "%s-%s".formatted(slug, eventToken);
        return "%s/%s.%s".formatted(
                prefix,
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
