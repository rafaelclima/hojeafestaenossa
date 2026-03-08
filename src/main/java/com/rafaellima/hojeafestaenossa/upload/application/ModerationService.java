package com.rafaellima.hojeafestaenossa.upload.application;

import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rafaellima.hojeafestaenossa.event.application.FindEventByIdService;
import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.shared.exception.NotFoundException;
import com.rafaellima.hojeafestaenossa.upload.domain.Upload;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;
import com.rafaellima.hojeafestaenossa.upload.web.SlideshowItemResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final UploadRepository uploadRepository;
    private final FindEventByIdService findEventByIdService;
    private final SimpMessagingTemplate messagingTemplate; // O "carteiro" do WebSocket

    @Transactional
    public void setVisibility(UUID uploadId, boolean isVisible) {
        // 1. Encontrar o upload que será moderado
        Upload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new NotFoundException("404", "Upload não encontrado"));

        // 2. Atualizar sua visibilidade e salvar no banco
        upload.setVisibility(isVisible);
        uploadRepository.save(upload);

        // 3. Se a mídia foi APROVADA, notificar o telão via WebSocket
        if (isVisible) {
            Event event = findEventByIdService.execute(upload.getEventId());

            var payload = new SlideshowItemResponse(
                    upload.getUrl(),
                    upload.getMediaType(),
                    upload.getMessage(),
                    upload.getCreatedAt());

            String topic = String.format("/topic/events/%s/slideshow", event.getAccessToken());
            messagingTemplate.convertAndSend(topic, payload);
        }
    }
}
