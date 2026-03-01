package com.rafaellima.hojeafestaenossa.upload.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rafaellima.hojeafestaenossa.event.application.FindEventByTokenService;
import com.rafaellima.hojeafestaenossa.upload.application.ListSlideshowUploadsService;
import com.rafaellima.hojeafestaenossa.upload.application.UploadMediaService;
import com.rafaellima.hojeafestaenossa.upload.domain.Upload;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    // TODO: Implementar o endpoint para upload de arquivos
    private final UploadMediaService uploadMediaService;
    private final FindEventByTokenService findEventByTokenService;
    private final ListSlideshowUploadsService listSlideshowUploadsService;

    @PostMapping
    public void upload(@RequestBody UploadRequest request) {
        // TODO: process POST request
        var event = findEventByTokenService.execute(request.eventToken());

        Upload upload = new Upload(
                event.getId(),
                request.mediaType(),
                request.storageKey(),
                request.originalName(),
                request.fileSize(),
                request.message());

        uploadMediaService.execute(upload);
    }

    @GetMapping("/events/{eventToken}/slideshow")
    public List<SlideshowItemResponse> getSlideshowItems(@PathVariable String eventToken) {
        var event = findEventByTokenService.execute(eventToken);

        return listSlideshowUploadsService.execute(event.getId()).stream().map(u -> new SlideshowItemResponse(
                u.getStorageKey(),
                u.getMediaType(),
                u.getMessage(),
                u.getCreatedAt()))
                .toList();

    }

}
