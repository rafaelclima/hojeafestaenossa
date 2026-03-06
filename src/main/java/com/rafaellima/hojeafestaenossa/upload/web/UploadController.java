package com.rafaellima.hojeafestaenossa.upload.web;

import java.io.IOException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rafaellima.hojeafestaenossa.event.application.FindEventByTokenService;
import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.shared.exception.NotFoundException;
import com.rafaellima.hojeafestaenossa.shared.exception.UnauthorizedException;
import com.rafaellima.hojeafestaenossa.upload.application.ListModerationAdminService;
import com.rafaellima.hojeafestaenossa.upload.application.ListSlideshowUploadsService;
import com.rafaellima.hojeafestaenossa.upload.application.ModerationService;
import com.rafaellima.hojeafestaenossa.upload.application.UploadMediaService;
import com.rafaellima.hojeafestaenossa.upload.domain.Upload;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadMediaService uploadMediaService;
    private final UploadRepository uploadRepository;
    private final FindEventByTokenService findEventByTokenService;
    private final ListSlideshowUploadsService listSlideshowUploadsService;
    private final ModerationService moderationService;
    private final ListModerationAdminService listModerationAdminService;

    @PostMapping(value = "/events/{eventToken}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(
            @PathVariable String eventToken,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "message", required = false) String message) throws IOException {

        Event event = findEventByTokenService.execute(eventToken);

        uploadMediaService.execute(file, event, message);

        return ResponseEntity.status(201).build();
    }

    @GetMapping("/events/{eventToken}/slideshow")
    public ResponseEntity<Page<SlideshowItemResponse>> getSlideshowItems(@PathVariable String eventToken,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        var event = findEventByTokenService.execute(eventToken);

        Page<SlideshowItemResponse> uploadedMedia = listSlideshowUploadsService.execute(event.getId(), page, size)
                .map(u -> new SlideshowItemResponse(
                        u.getUrl(),
                        u.getMediaType(),
                        u.getMessage(),
                        u.getCreatedAt()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(uploadedMedia);
    }

    @PutMapping("/{uploadId}/visibility")
    public ResponseEntity<Void> setVisbility(
            @PathVariable UUID uploadId,
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestBody VisibilityRequest request) {

        Upload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new NotFoundException("404", "Upload não encontrado"));

        Event event = findEventByTokenService.execute(upload.getEventId().toString());

        if (!event.getAdminToken().equals(adminToken)) {
            throw new UnauthorizedException("401", "Invalid admin token");
        }

        moderationService.setVisibility(uploadId, request.visible());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/uploads/events/{eventToken}/moderation")
    public ResponseEntity<Page<ModerationItemResponse>> getModerationItems(
            @PathVariable String eventToken,
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size

    ) {
        Event event = findEventByTokenService.execute(eventToken);
        if (!event.getAdminToken().equals(adminToken)) {
            throw new UnauthorizedException("401", "Invalid admin token");
        }
        Page<ModerationItemResponse> uploads = listModerationAdminService.execute(event.getId(), page, size)
                .map(u -> new ModerationItemResponse(
                        u.getId(),
                        u.getUrl(),
                        u.getMediaType(),
                        u.getMessage(),
                        u.getCreatedAt(),
                        u.isVisible()));

        return ResponseEntity.status(HttpStatus.OK).body(uploads);

    };

}
