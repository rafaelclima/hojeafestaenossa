package com.rafaellima.hojeafestaenossa.event.application;

import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.shared.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteEventService {

    private final EventRepository eventRepository;

    public void execute(String token) {
        Event event = eventRepository.findByAccessToken(token)
                .orElseThrow(() -> new NotFoundException("404", "Event not found"));

        eventRepository.delete(event);
    }

}