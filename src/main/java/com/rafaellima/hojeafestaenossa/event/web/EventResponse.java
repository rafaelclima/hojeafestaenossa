package com.rafaellima.hojeafestaenossa.event.web;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
                UUID id,
                String name,
                Instant startedAt,
                Instant expiredAt,
                Instant createdAt,
                String accessToken,
                boolean publicAlbum) {

}
