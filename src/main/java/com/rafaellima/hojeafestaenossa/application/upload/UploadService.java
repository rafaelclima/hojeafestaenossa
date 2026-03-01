package com.rafaellima.hojeafestaenossa.application.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rafaellima.hojeafestaenossa.domain.post.FileType;
import com.rafaellima.hojeafestaenossa.domain.post.Post;
import com.rafaellima.hojeafestaenossa.domain.post.PostRepository;
import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.infra.storage.StorageService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UploadService {

    private final EventRepository eventRepository;
    private final PostRepository postRepository;
    private final StorageService storageService;

    public void upload(
            String eventSlug,
            MultipartFile file,
            String message) throws IOException {

        // 1️⃣ validar evento
        Event event = eventRepository.findBySlug(eventSlug)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        // 2️⃣ detectar tipo
        FileType fileType = resolveFileType(file);

        // 3️⃣ gerar nome seguro
        String objectName = generateObjectName(eventSlug, file);

        // 4️⃣ upload OCI (streaming)
        String fileUrl;

        try (InputStream is = file.getInputStream()) {
            fileUrl = storageService.upload(
                    objectName,
                    is,
                    file.getSize(),
                    file.getContentType());
        }

        // 5️⃣ persistir post
        Post post = new Post();
        post.setEvent(event);
        post.setFileUrl(fileUrl);
        post.setFileType(fileType);
        post.setMessage(message);

        postRepository.save(post);
    }

    private FileType resolveFileType(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null)
            throw new RuntimeException("Tipo de arquivo inválido");

        if (contentType.startsWith("image"))
            return FileType.IMAGE;

        if (contentType.startsWith("video"))
            return FileType.VIDEO;

        throw new RuntimeException("Tipo não suportado");
    }

    private String generateObjectName(
            String eventSlug,
            MultipartFile file) {

        String extension = getExtension(file.getOriginalFilename());

        return "%s/%s.%s".formatted(
                eventSlug,
                UUID.randomUUID(),
                extension);
    }

    private String getExtension(String filename) {

        if (filename == null || !filename.contains("."))
            return "bin";

        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
