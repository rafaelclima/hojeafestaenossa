package com.rafaellima.hojeafestaenossa.upload.web;

import com.rafaellima.hojeafestaenossa.upload.domain.MediaType;

public record UploadRequest(
        String eventToken,
        MediaType mediaType,
        String storageKey,
        String originalName,
        long fileSize,
        String message) {

}
