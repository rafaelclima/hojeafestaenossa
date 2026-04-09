package com.rafaellima.hojeafestaenossa.event.application;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.event.web.ManifestExport;
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
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd_HHmmss")
            .withZone(ZoneId.of("UTC"));

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    public void exportToZip(UUID eventId, OutputStream outputStream) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("404", "Evento não encontrado"));

        List<Upload> uploads = uploadRepository.findByEventIdOrderByMediaTypeAscCreatedAtAsc(eventId);

        log.info("Iniciando exportação de {} mídias do evento: {}", uploads.size(), event.getName());

        List<ManifestExport.FileEntry> fileEntries = new ArrayList<>();
        int photoCount = 0;
        int videoCount = 0;
        long totalSize = 0;

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
                    photoCount++;
                } else {
                    fileName = folder + String.format("%03d_%s.%s", videoIndex++, timestamp, extension);
                    videoCount++;
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

                fileEntries.add(new ManifestExport.FileEntry(
                        fileName,
                        upload.getMediaType().name(),
                        upload.getMessage(),
                        upload.getCreatedAt(),
                        upload.getFileSize()));

                totalSize += upload.getFileSize();
            }

            ManifestExport manifest = new ManifestExport(
                    event.getName(),
                    event.getAccessToken(),
                    Instant.now(),
                    uploads.size(),
                    new ManifestExport.Summary(photoCount, videoCount, totalSize / (1024.0 * 1024.0)),
                    fileEntries);

            ZipEntry manifestEntry = new ZipEntry("manifest.json");
            zos.putNextEntry(manifestEntry);
            zos.write(objectMapper.writeValueAsBytes(manifest));
            zos.closeEntry();
            log.debug("manifest.json adicionado ao ZIP");

            String html = generateGalleryHtml(event, manifest);
            ZipEntry htmlEntry = new ZipEntry("galeria.html");
            zos.putNextEntry(htmlEntry);
            zos.write(html.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            log.debug("galeria.html adicionado ao ZIP");
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

    private String generateGalleryHtml(Event event, ManifestExport manifest) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"pt-BR\">\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("    <title>").append(escapeHtml(event.getName())).append("</title>\n");
        sb.append("    <style>\n");
        sb.append("        * { margin: 0; padding: 0; box-sizing: border-box; }\n");
        sb.append("        body { font-family: system-ui, -apple-system, sans-serif; background: #0f172a; color: #e2e8f0; min-height: 100vh; }\n");
        sb.append("        .header { text-align: center; padding: 2rem; border-bottom: 1px solid #334155; }\n");
        sb.append("        .header h1 { font-size: 2rem; margin-bottom: 0.5rem; }\n");
        sb.append("        .stats { color: #94a3b8; font-size: 0.9rem; margin-top: 0.25rem; }\n");
        sb.append("        .gallery { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; padding: 1rem; }\n");
        sb.append("        .item { cursor: pointer; position: relative; aspect-ratio: 1; overflow: hidden; border-radius: 0.5rem; background: #1e293b; }\n");
        sb.append("        .item img, .item video { width: 100%; height: 100%; object-fit: cover; }\n");
        sb.append("        .item .overlay { position: absolute; bottom: 0; left: 0; right: 0; padding: 0.5rem; background: linear-gradient(transparent, rgba(0,0,0,0.8)); }\n");
        sb.append("        .item .message { font-size: 0.75rem; color: #fff; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }\n");
        sb.append("        .item .badge { position: absolute; top: 0.5rem; right: 0.5rem; background: rgba(0,0,0,0.6); padding: 0.25rem 0.5rem; border-radius: 0.25rem; font-size: 0.7rem; }\n");
        sb.append("        .lightbox { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.95); z-index: 1000; align-items: center; justify-content: center; flex-direction: column; padding: 1rem; }\n");
        sb.append("        .lightbox.active { display: flex; }\n");
        sb.append("        .lightbox-media { max-width: 90vw; max-height: 75vh; object-fit: contain; border-radius: 0.5rem; }\n");
        sb.append("        .lightbox-info { max-width: 600px; margin-top: 1rem; padding: 1rem; background: #1e293b; border-radius: 0.5rem; text-align: center; width: 100%; }\n");
        sb.append("        .lightbox-message { font-size: 1rem; margin-bottom: 0.5rem; }\n");
        sb.append("        .lightbox-date { font-size: 0.85rem; color: #94a3b8; }\n");
        sb.append("        .close { position: fixed; top: 1rem; right: 1rem; font-size: 2.5rem; cursor: pointer; color: #fff; z-index: 1001; width: 3rem; height: 3rem; display: flex; align-items: center; justify-content: center; }\n");
        sb.append("        .nav { position: fixed; top: 50%; transform: translateY(-50%); font-size: 2rem; cursor: pointer; color: #fff; padding: 1rem; z-index: 1001; }\n");
        sb.append("        .nav.prev { left: 0; }\n");
        sb.append("        .nav.next { right: 0; }\n");
        sb.append("        @media (max-width: 600px) { .header h1 { font-size: 1.25rem; } .gallery { grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); } }\n");
        sb.append("    </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("    <header class=\"header\">\n");
        sb.append("        <h1>").append(escapeHtml(event.getName())).append("</h1>\n");
        sb.append("        <p class=\"stats\">").append(manifest.totalFiles()).append(" mídias • ")
          .append(manifest.summary().photos()).append(" fotos • ")
          .append(manifest.summary().videos()).append(" vídeos • ")
          .append(String.format("%.1f", manifest.summary().totalSizeMB())).append(" MB</p>\n");
        sb.append("        <p class=\"stats\">Exportado em ").append(DISPLAY_FORMATTER.format(manifest.exportedAt())).append("</p>\n");
        sb.append("    </header>\n");
        sb.append("    \n");
        sb.append("    <main class=\"gallery\" id=\"gallery\">\n");

        int index = 0;
        for (ManifestExport.FileEntry entry : manifest.files()) {
            boolean isVideo = "VIDEO".equals(entry.type());
            sb.append("        <div class=\"item\" data-index=\"").append(index).append("\" onclick=\"openLightbox(").append(index).append(")\">\n");
            
            if (isVideo) {
                sb.append("            <video src=\"").append(entry.fileName()).append("\" muted preload=\"metadata\"></video>\n");
                sb.append("            <span class=\"badge\">🎬</span>\n");
            } else {
                sb.append("            <img src=\"").append(entry.fileName()).append("\" alt=\"Foto\" loading=\"lazy\">\n");
            }
            
            if (entry.message() != null && !entry.message().isBlank()) {
                sb.append("            <div class=\"overlay\">\n");
                sb.append("                <p class=\"message\">").append(escapeHtml(entry.message())).append("</p>\n");
                sb.append("            </div>\n");
            }
            
            sb.append("        </div>\n");
            index++;
        }

        sb.append("    </main>\n");
        sb.append("    \n");
        sb.append("    <div class=\"lightbox\" id=\"lightbox\">\n");
        sb.append("        <span class=\"close\" onclick=\"closeLightbox()\">×</span>\n");
        sb.append("        <span class=\"nav prev\" onclick=\"prevItem()\">‹</span>\n");
        sb.append("        <span class=\"nav next\" onclick=\"nextItem()\">›</span>\n");
        sb.append("        <img class=\"lightbox-media\" id=\"lightbox-img\">\n");
        sb.append("        <video class=\"lightbox-media\" id=\"lightbox-video\" controls style=\"display:none;\"></video>\n");
        sb.append("        <div class=\"lightbox-info\">\n");
        sb.append("            <p class=\"lightbox-message\" id=\"lightbox-message\"></p>\n");
        sb.append("            <p class=\"lightbox-date\" id=\"lightbox-date\"></p>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");
        sb.append("    \n");
        sb.append("    <script>\n");
        try {
            sb.append("        const files = ").append(objectMapper.writeValueAsString(manifest.files())).append(";\n");
        } catch (Exception e) {
            sb.append("        const files = [];\n");
            log.error("Erro ao serializar arquivos para JSON", e);
        }
        sb.append("        let currentIndex = 0;\n");
        sb.append("        \n");
        sb.append("        function openLightbox(index) {\n");
        sb.append("            currentIndex = index;\n");
        sb.append("            showItem(index);\n");
        sb.append("            document.getElementById('lightbox').classList.add('active');\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        function closeLightbox() {\n");
        sb.append("            document.getElementById('lightbox').classList.remove('active');\n");
        sb.append("            document.getElementById('lightbox-video').pause();\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        function showItem(index) {\n");
        sb.append("            const item = files[index];\n");
        sb.append("            const img = document.getElementById('lightbox-img');\n");
        sb.append("            const video = document.getElementById('lightbox-video');\n");
        sb.append("            \n");
        sb.append("            if (item.type === 'VIDEO') {\n");
        sb.append("                img.style.display = 'none';\n");
        sb.append("                video.style.display = 'block';\n");
        sb.append("                video.src = item.fileName;\n");
        sb.append("            } else {\n");
        sb.append("                video.style.display = 'none';\n");
        sb.append("                img.style.display = 'block';\n");
        sb.append("                img.src = item.fileName;\n");
        sb.append("            }\n");
        sb.append("            \n");
        sb.append("            document.getElementById('lightbox-message').textContent = item.message || 'Sem mensagem';\n");
        sb.append("            document.getElementById('lightbox-date').textContent = formatDateTime(item.createdAt);\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        function prevItem() {\n");
        sb.append("            currentIndex = (currentIndex - 1 + files.length) % files.length;\n");
        sb.append("            showItem(currentIndex);\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        function nextItem() {\n");
        sb.append("            currentIndex = (currentIndex + 1) % files.length;\n");
        sb.append("            showItem(currentIndex);\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        function formatDateTime(iso) {\n");
        sb.append("            const date = new Date(iso);\n");
        sb.append("            return date.toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        document.addEventListener('keydown', (e) => {\n");
        sb.append("            if (!document.getElementById('lightbox').classList.contains('active')) return;\n");
        sb.append("            if (e.key === 'Escape') closeLightbox();\n");
        sb.append("            if (e.key === 'ArrowLeft') prevItem();\n");
        sb.append("            if (e.key === 'ArrowRight') nextItem();\n");
        sb.append("        });\n");
        sb.append("    </script>\n");
        sb.append("</body>\n");
        sb.append("</html>");
        
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}