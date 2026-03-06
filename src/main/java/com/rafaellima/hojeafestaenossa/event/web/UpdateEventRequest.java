package com.rafaellima.hojeafestaenossa.event.web;

import java.time.Instant;

public record UpdateEventRequest(
        String name,
        Instant startedAt,
        Instant expiredAt,
        Boolean isPublic) {

}
