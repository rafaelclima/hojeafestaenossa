package com.rafaellima.hojeafestaenossa.upload.web;

import java.time.Instant;
import java.util.UUID;

import com.rafaellima.hojeafestaenossa.upload.domain.MediaType;

public record ModerationItemResponse(
                UUID id,
                String url,
                MediaType mediaType,
                String thumbnailUrl,
                String message,
                Instant createdAt,
                boolean visible) {

}
