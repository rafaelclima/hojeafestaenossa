package com.rafaellima.hojeafestaenossa.web.upload;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rafaellima.hojeafestaenossa.application.upload.UploadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events/{slug}/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(
            @PathVariable String slug,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "message", required = false) String message) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        uploadService.upload(slug, file, message);

        return ResponseEntity.status(201).build();
    }
}
