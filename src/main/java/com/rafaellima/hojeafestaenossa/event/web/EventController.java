package com.rafaellima.hojeafestaenossa.event.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rafaellima.hojeafestaenossa.event.application.CreateEventService;
import com.rafaellima.hojeafestaenossa.event.application.DeleteEventService;
import com.rafaellima.hojeafestaenossa.event.application.FindAllEventsService;
import com.rafaellima.hojeafestaenossa.event.application.FindEventByTokenService;
import com.rafaellima.hojeafestaenossa.event.application.UpdateEventService;
import com.rafaellima.hojeafestaenossa.event.domain.Event;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final FindEventByTokenService findEventByTokenService;
    private final FindAllEventsService findAllEventsService;
    private final CreateEventService createEventService;
    private final UpdateEventService updateEventService;
    private final DeleteEventService deleteEventService;

    @GetMapping("/{token}")
    public EventResponse getEvent(@PathVariable String token) {

        Event event = findEventByTokenService.execute(token);
        return new EventResponse(event.getId(), event.getName(), event.getStartedAt(), event.getExpiredAt(),
                event.getCreatedAt(), event.getAccessToken(), event.isPublicAlbum());
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody CreateEventRequest request) {
        Event event = createEventService.execute(request);

        EventResponse response = new EventResponse(
                event.getId(),
                event.getName(),
                event.getStartedAt(),
                event.getExpiredAt(),
                event.getCreatedAt(),
                event.getAccessToken(),
                event.isPublicAlbum());

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<EventResponse>> listEvents(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Page<Event> events = findAllEventsService.execute(PageRequest.of(page, size));
        Page<EventResponse> response = events.map(event -> new EventResponse(
                event.getId(),
                event.getName(),
                event.getStartedAt(),
                event.getExpiredAt(),
                event.getCreatedAt(),
                event.getAccessToken(),
                event.isPublicAlbum()));

        return ResponseEntity.ok(response);

    }

    @PutMapping("/{token}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable String token, @RequestBody UpdateEventRequest request) {

        Event event = updateEventService.execute(token, request);

        EventResponse response = new EventResponse(
                event.getId(),
                event.getName(),
                event.getStartedAt(),
                event.getExpiredAt(),
                event.getCreatedAt(),
                event.getAccessToken(),
                event.isPublicAlbum());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String token) {
        deleteEventService.execute(token);
        return ResponseEntity.noContent().build();
    }

}
