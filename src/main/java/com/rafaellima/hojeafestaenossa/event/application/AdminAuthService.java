package com.rafaellima.hojeafestaenossa.event.application;

import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.shared.exception.NotFoundException;
import com.rafaellima.hojeafestaenossa.shared.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final EventRepository eventRepository;

    public void validateAdminToken(String eventToken, String adminToken) {

        Event event = eventRepository.findByAccessToken(eventToken)
                .orElseThrow(() -> new NotFoundException("404", "Event not found"));

        if (!event.getAdminToken().equals(adminToken)) {
            throw new UnauthorizedException("401", "Invalid admin token");
        }

    }

}
