package com.rafaellima.hojeafestaenossa.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rafaellima.hojeafestaenossa.event.application.FindEventByTokenService;
import com.rafaellima.hojeafestaenossa.event.domain.Event;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final FindEventByTokenService findEventByTokenService;

    @GetMapping("/{token}")
    public EventResponse getEvent(@PathVariable String token) {

        Event event = findEventByTokenService.execute(token);
        return new EventResponse(event.getId(), event.getAccessToken(), event.isPublicAlbum());
    }

}
