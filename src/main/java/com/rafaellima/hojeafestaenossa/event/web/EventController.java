package com.rafaellima.hojeafestaenossa.event.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rafaellima.hojeafestaenossa.event.application.AdminAuthService;
import com.rafaellima.hojeafestaenossa.event.application.CreateEventService;
import com.rafaellima.hojeafestaenossa.event.application.DeleteEventService;
import com.rafaellima.hojeafestaenossa.event.application.FindAllEventsService;
import com.rafaellima.hojeafestaenossa.event.application.FindEventByTokenService;
import com.rafaellima.hojeafestaenossa.event.application.GetEventStatsService;
import com.rafaellima.hojeafestaenossa.event.application.UpdateEventService;
import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.shared.config.AppProperties;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final FindEventByTokenService findEventByTokenService;
    private final FindAllEventsService findAllEventsService;
    private final CreateEventService createEventService;
    private final UpdateEventService updateEventService;
    private final DeleteEventService deleteEventService;
    private final AppProperties appProperties;
    private final AdminAuthService adminAuthService;
    private final GetEventStatsService getEventStatsService;

    @GetMapping("/{token}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable String token) {

        Event event = findEventByTokenService.execute(token);
        String baseUrl = appProperties.getBaseUrl();
        String eventUrl = baseUrl + "/events?eventId=" + event.getAccessToken();

        EventResponse response = EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .startedAt(event.getStartedAt())
                .expiredAt(event.getExpiredAt())
                .createdAt(event.getCreatedAt())
                .accessToken(event.getAccessToken())
                .publicAlbum(event.isPublicAlbum())
                .eventUrl(eventUrl)
                .build();

        return ResponseEntity.ok(response);

    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody CreateEventRequest request) {
        Event event = createEventService.execute(request);
        String baseUrl = appProperties.getBaseUrl();
        String eventUrl = baseUrl + "/events?eventId=" + event.getAccessToken();

        EventResponse response = EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .startedAt(event.getStartedAt())
                .expiredAt(event.getExpiredAt())
                .createdAt(event.getCreatedAt())
                .accessToken(event.getAccessToken())
                .publicAlbum(event.isPublicAlbum())
                .eventUrl(eventUrl)
                .build();

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<EventResponse>> listEvents(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Page<Event> events = findAllEventsService.execute(PageRequest.of(page, size));
        String baseUrl = appProperties.getBaseUrl();
        String eventUrl = baseUrl + "/events?eventId=";

        Page<EventResponse> response = events.map(event -> EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .startedAt(event.getStartedAt())
                .expiredAt(event.getExpiredAt())
                .createdAt(event.getCreatedAt())
                .accessToken(event.getAccessToken())
                .publicAlbum(event.isPublicAlbum())
                .eventUrl(eventUrl + event.getAccessToken())
                .build());

        return ResponseEntity.ok(response);

    }

    @PutMapping("/{token}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable String token,
            @RequestHeader("X-Admin-Token") String adminToken,
            @RequestBody UpdateEventRequest request) {

        adminAuthService.validateAdminToken(token, adminToken);

        Event event = updateEventService.execute(token, request);
        String baseUrl = appProperties.getBaseUrl();
        String eventUrl = baseUrl + "/events?eventId=" + event.getAccessToken();

        EventResponse response = EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .startedAt(event.getStartedAt())
                .expiredAt(event.getExpiredAt())
                .createdAt(event.getCreatedAt())
                .accessToken(event.getAccessToken())
                .publicAlbum(event.isPublicAlbum())
                .eventUrl(eventUrl)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> deleteEvent(
            @RequestHeader("X-Admin-Token") String adminToken,
            @PathVariable String token) {
        adminAuthService.validateAdminToken(token, adminToken);
        deleteEventService.execute(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{token}/stats")
    public ResponseEntity<EventStatsResponse> getEventStats(
            @PathVariable String token,
            @RequestHeader("X-Admin-Token") String adminToken) {

        adminAuthService.validateAdminToken(token, adminToken);
        EventStatsResponse stats = getEventStatsService.execute(token);
        return ResponseEntity.ok(stats);
    }

}
