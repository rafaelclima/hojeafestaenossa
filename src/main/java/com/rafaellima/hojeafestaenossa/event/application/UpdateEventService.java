package com.rafaellima.hojeafestaenossa.event.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.event.web.UpdateEventRequest;
import com.rafaellima.hojeafestaenossa.shared.exception.FutureDateException;
import com.rafaellima.hojeafestaenossa.shared.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateEventService {

    private final EventRepository eventRepository;

    public Event execute(String token, UpdateEventRequest request) {
        Event event = eventRepository.findByAccessToken(token)
                .orElseThrow(() -> new NotFoundException("404", "Event not found"));

        if (request.name() != null) {
            event.setName(request.name());
        }

        if (request.startedAt() != null) {
            event.setStartedAt(request.startedAt());
        }

        if (request.expiredAt() != null) {
            event.setExpiredAt(request.expiredAt());
        }

        if (request.isPublic() != null) {
            event.setPublicAlbum(request.isPublic());
        }

        if (event.getExpiredAt().isBefore(event.getStartedAt())) {
            throw new FutureDateException("400",
                    "Data de expiração está no futuro em relação a data de inicio do evento!!");
        }

        return eventRepository.save(event);
    }

}
