package com.rafaellima.hojeafestaenossa.event.web;

import java.time.Instant;
import java.util.UUID;

public record AdminEventItemResponse(
    UUID id,
    String name,
    String accessToken,
    Instant startedAt,
    Instant expiredAt,
    Instant createdAt,
    String status,
    UploadsSummary uploads,
    double storageUsedMB
) {
    public record UploadsSummary(
        long total,
        long photos,
        long videos
    ) {}
}