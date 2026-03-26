package com.rafaellima.hojeafestaenossa.event.application;

import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.infra.storage.StorageService;
import com.rafaellima.hojeafestaenossa.shared.exception.NotFoundException;
import com.rafaellima.hojeafestaenossa.shared.util.SlugUtils;
import com.rafaellima.hojeafestaenossa.upload.domain.MediaType;
import com.rafaellima.hojeafestaenossa.upload.domain.Upload;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportEventMediaService {

    private final EventRepository eventRepository;
    private final UploadRepository uploadRepository;
    private final StorageService storageService;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd_HHmmss")
            .withZone(ZoneId.of("UTC"));

    public void exportToZip(UUID eventId, OutputStream outputStream) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("404", "Evento não encontrado"));

        List<Upload> uploads = uploadRepository.findByEventIdOrderByMediaTypeAscCreatedAtAsc(eventId);

        log.info("Iniciando exportação de {} mídias do evento: {}", uploads.size(), event.getName());

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            int photoIndex = 1;
            int videoIndex = 1;

            for (Upload upload : uploads) {
                String folder = upload.getMediaType() == MediaType.PHOTO ? "fotos/" : "videos/";
                String extension = getExtension(upload.getStorageKey());
                String timestamp = TIMESTAMP_FORMATTER.format(upload.getCreatedAt());

                String fileName;
                if (upload.getMediaType() == MediaType.PHOTO) {
                    fileName = folder + String.format("%03d_%s.%s", photoIndex++, timestamp, extension);
                } else {
                    fileName = folder + String.format("%03d_%s.%s", videoIndex++, timestamp, extension);
                }

                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);

                try {
                    storageService.download(upload.getStorageKey(), zos);
                    log.debug("Arquivo adicionado ao ZIP: {}", fileName);
                } catch (Exception e) {
                    log.error("Erro ao baixar arquivo {}: {}", upload.getStorageKey(), e.getMessage());
                }

                zos.closeEntry();
            }
        }

        log.info("Exportação concluída para o evento: {}", event.getName());
    }

    public String generateZipFileName(Event event) {
        String slug = SlugUtils.slugify(event.getName());
        String date = java.time.LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return "evento-%s-%s.zip".formatted(slug, date);
    }

    private String getExtension(String storageKey) {
        if (storageKey == null || !storageKey.contains(".")) {
            return "bin";
        }
        return storageKey.substring(storageKey.lastIndexOf('.') + 1);
    }
}