package com.rafaellima.hojeafestaenossa.event.web;

import java.util.List;

public record TopEventsResponse(
    List<EventData> data
) {
    public record EventData(
        String eventName,
        long uploads,
        long photos,
        long videos
    ) {}
}