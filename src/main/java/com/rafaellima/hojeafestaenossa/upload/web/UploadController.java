package com.rafaellima.hojeafestaenossa.upload.web;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rafaellima.hojeafestaenossa.event.application.AdminAuthService;
import com.rafaellima.hojeafestaenossa.event.application.FindEventByIdService;
import com.rafaellima.hojeafestaenossa.event.application.FindEventByTokenService;
import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.shared.exception.BusinessException;
import com.rafaellima.hojeafestaenossa.shared.exception.MaxUploadSizeExceededException;
import com.rafaellima.hojeafestaenossa.shared.exception.NotFoundException;
import com.rafaellima.hojeafestaenossa.upload.application.DeleteUploadService;
import com.rafaellima.hojeafestaenossa.upload.application.ListModerationAdminService;
import com.rafaellima.hojeafestaenossa.upload.application.ListSlideshowUploadsService;
import com.rafaellima.hojeafestaenossa.upload.application.ModerationService;
import com.rafaellima.hojeafestaenossa.upload.application.UploadMediaService;
import com.rafaellima.hojeafestaenossa.upload.domain.Upload;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

        private final UploadMediaService uploadMediaService;
        private final UploadRepository uploadRepository;
        private final FindEventByTokenService findEventByTokenService;
        private final FindEventByIdService findEventByIdService;
        private final AdminAuthService adminAuthService;
        private final ListSlideshowUploadsService listSlideshowUploadsService;
        private final ModerationService moderationService;
        private final ListModerationAdminService listModerationAdminService;
        private final DeleteUploadService deleteUploadService;

        @PostMapping(value = "/events/{eventToken}", consumes = "multipart/form-data")
        @ResponseStatus(HttpStatus.ACCEPTED)
        public void upload(
                        @PathVariable String eventToken,
                        @RequestPart("file") MultipartFile file,
                        @RequestPart(value = "message", required = false) String message) throws IOException {

                Event event = findEventByTokenService.execute(eventToken);
                validateFile(file);
                File tempFile = saveTempFile(file);

                uploadMediaService.execute(
                                tempFile,
                                file.getOriginalFilename(),
                                file.getContentType(),
                                file.getSize(),
                                event,
                                message);
        }

        private File saveTempFile(MultipartFile file) throws IOException {
                String originalFilename = file.getOriginalFilename();
                String prefix = "upload-";
                String suffix = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                        suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                File tempFile = File.createTempFile(prefix, suffix);
                file.transferTo(tempFile);
                return tempFile;
        }

        private void validateFile(MultipartFile file) {
                if (file == null || file.isEmpty()) {
                        throw new BusinessException("400", "Arquivo vazio");
                }
                String contentType = file.getContentType();
                if (contentType == null) {
                        throw new BusinessException("400", "Tipo de arquivo inválido");
                }
                boolean isImage = contentType.startsWith("image/");
                boolean isVideo = contentType.startsWith("video/");
                if (!isImage && !isVideo) {
                        throw new BusinessException("400",
                                        "Tipo de arquivo não suportado. Envie apenas fotos ou vídeos.");
                }
                if ((isImage && file.getSize() > 8388608) || (isVideo && file.getSize() > 52428800)) {
                        throw new MaxUploadSizeExceededException();
                }
        }

        @GetMapping("/events/{eventToken}/slideshow")
        public ResponseEntity<Page<SlideshowItemResponse>> getSlideshowItems(@PathVariable String eventToken,
                        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
                var event = findEventByTokenService.execute(eventToken);

                Page<SlideshowItemResponse> uploadedMedia = listSlideshowUploadsService
                                .execute(event.getId(), page, size)
                                .map(u -> new SlideshowItemResponse(
                                                u.getUrl(),
                                                u.getMediaType(),
                                                u.getMessage(),
                                                u.getCreatedAt()));

                return ResponseEntity.status(HttpStatus.OK)
                                .body(uploadedMedia);
        }

        @PutMapping("/events/{eventToken}/{uploadId}/visibility")
        public ResponseEntity<Void> setVisibility(
                        @PathVariable String eventToken,
                        @PathVariable UUID uploadId,
                        @RequestHeader("X-Admin-Token") String adminToken,
                        @RequestBody VisibilityRequest request) {

                adminAuthService.validateAdminToken(eventToken, adminToken);

                Event event = findEventByTokenService.execute(eventToken);

                Upload upload = uploadRepository.findById(uploadId)
                                .orElseThrow(() -> new NotFoundException("404", "Upload não encontrado"));

                if (!upload.getEventId().equals(event.getId())) {
                        throw new BusinessException("403", "Upload não pertence a este evento");
                }

                moderationService.setVisibility(uploadId, request.visible());
                return ResponseEntity.ok().build();
        }

        @GetMapping("/events/{eventToken}/moderation")
        public ResponseEntity<Page<ModerationItemResponse>> getModerationItems(
                        @PathVariable String eventToken,
                        @RequestHeader("X-Admin-Token") String adminToken,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "50") int size

        ) {
                adminAuthService.validateAdminToken(eventToken, adminToken);
                Event event = findEventByTokenService.execute(eventToken);
                Page<ModerationItemResponse> uploads = listModerationAdminService.execute(event.getId(), page, size)
                                .map(u -> new ModerationItemResponse(
                                                u.getId(),
                                                u.getUrl(),
                                                u.getMediaType(),
                                                u.getThumbnailUrl(),
                                                u.getMessage(),
                                                u.getCreatedAt(),
                                                u.isVisible()));

                return ResponseEntity.status(HttpStatus.OK).body(uploads);

        };

        @DeleteMapping("/{eventToken}/{uploadId}")
        public ResponseEntity<Void> deleteUpload(
                        @PathVariable String eventToken,
                        @PathVariable UUID uploadId,
                        @RequestHeader("X-Admin-Token") String adminToken) {

                adminAuthService.validateAdminToken(eventToken, adminToken);
                Event event = findEventByTokenService.execute(eventToken);

                deleteUploadService.execute(uploadId, event.getId());
                return ResponseEntity.noContent().build();

        }

}
