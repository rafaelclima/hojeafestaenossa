package com.rafaellima.hojeafestaenossa.event.web;

import java.time.Instant;

public record CreateEventRequest(
        String token,
        String name,
        Instant startedAt,
        Instant expiredAt,
        Boolean isPublic) {

}
