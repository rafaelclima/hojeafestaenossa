package com.rafaellima.hojeafestaenossa.event.application;

import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.shared.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindEventByTokenService {

    private final EventRepository eventRepository;

    public Event execute(String accessToken) {
        return eventRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new NotFoundException("404", "Evento não encontrado"));
    }

}
