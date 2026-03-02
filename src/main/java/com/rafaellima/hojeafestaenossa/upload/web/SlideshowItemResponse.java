package com.rafaellima.hojeafestaenossa.upload.web;

import java.time.Instant;

import com.rafaellima.hojeafestaenossa.upload.domain.MediaType;

public record SlideshowItemResponse(
        String url,
        MediaType mediaType,
        String message,
        Instant createdAt) {

}
