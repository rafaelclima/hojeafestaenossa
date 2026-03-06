package com.rafaellima.hojeafestaenossa.event.web;

import java.time.Instant;
import java.util.UUID;

import com.rafaellima.hojeafestaenossa.event.domain.Event;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EventResponse {

    private UUID id;
    private String name;
    private Instant startedAt;
    private Instant expiredAt;
    private Instant createdAt;
    private String accessToken;
    private boolean publicAlbum;
    private String eventUrl;

    public static EventResponse from(Event event, String baseUrl) {
        return EventResponse.builder()

                .id(event.getId())
                .name(event.getName())
                .startedAt(event.getStartedAt())
                .expiredAt(event.getExpiredAt())
                .createdAt(event.getCreatedAt())
                .accessToken(event.getAccessToken())
                .publicAlbum(event.isPublicAlbum())
                .eventUrl(baseUrl + "/events?eventId=" + event.getAccessToken())
                .build();

    }

}
