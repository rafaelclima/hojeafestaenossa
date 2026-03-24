package com.rafaellima.hojeafestaenossa.event.web;

import java.time.Instant;
import java.util.UUID;

public record EventTokenResponse(
    String token,
    Instant createdAt,
    Instant usedAt,
    UUID eventId
) {
    public EventTokenResponse(String token, Instant createdAt) {
        this(token, createdAt, null, null);
    }
}