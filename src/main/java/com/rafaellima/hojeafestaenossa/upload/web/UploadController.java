package com.rafaellima.hojeafestaenossa.upload.web;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rafaellima.hojeafestaenossa.event.application.FindEventByTokenService;
import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.upload.application.ListSlideshowUploadsService;
import com.rafaellima.hojeafestaenossa.upload.application.UploadMediaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadMediaService uploadMediaService;
    private final FindEventByTokenService findEventByTokenService;
    private final ListSlideshowUploadsService listSlideshowUploadsService;

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
    public List<SlideshowItemResponse> getSlideshowItems(@PathVariable String eventToken) {
        var event = findEventByTokenService.execute(eventToken);

        return listSlideshowUploadsService.execute(event.getId()).stream()
                .map(u -> new SlideshowItemResponse(
                        u.getUrl(),
                        u.getMediaType(),
                        u.getMessage(),
                        u.getCreatedAt()))
                .toList();
    }
}
