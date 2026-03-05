package com.rafaellima.hojeafestaenossa.event.web;

import java.time.Instant;

public record CreateEventRequest(
        String name,
        Instant startedAt,
        Instant expiredAt,
        Boolean isPublic) {

}
