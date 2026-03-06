package com.rafaellima.hojeafestaenossa.event.application;

import java.security.SecureRandom;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.event.web.CreateEventRequest;
import com.rafaellima.hojeafestaenossa.shared.exception.FutureDateException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateEventService {

    private final EventRepository eventRepository;

    public Event execute(CreateEventRequest request) {

        if (request.expiredAt().isBefore(request.startedAt())) {
            throw new FutureDateException("400",
                    "Data de expiracao está no futuro em relacao a data de inicio do evento!!");
        }

        String generateAccessToken = UUID.randomUUID().toString();
        String generateAdminToken = RandomStringUtils.random(6, 0, 0, true, true, null, new SecureRandom());

        Event event = new Event(request.name(), generateAccessToken, request.startedAt(), request.expiredAt(),
                generateAdminToken);

        return eventRepository.save(event);

    }

}
