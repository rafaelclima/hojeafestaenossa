package com.rafaellima.hojeafestaenossa.event.application;

import java.security.SecureRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.domain.EventToken;
import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.event.repository.EventTokenRepository;
import com.rafaellima.hojeafestaenossa.event.web.CreateEventRequest;
import com.rafaellima.hojeafestaenossa.shared.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateEventService {

    private final EventRepository eventRepository;
    private final EventTokenRepository tokenRepository;

    public Event execute(CreateEventRequest request) {
        if (request.token() == null || request.token().isBlank()) {
            throw new BusinessException("400", "Token é obrigatório");
        }

        EventToken eventToken = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BusinessException("404", "Token inválido"));

        if (eventToken.isUsed()) {
            throw new BusinessException("400", "Token já foi utilizado");
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new BusinessException("400", "Nome do evento é obrigatório");
        }

        if (request.name().length() > 150) {
            throw new BusinessException("400", "Nome do evento deve ter no máximo 150 caracteres");
        }

        if (request.startedAt() == null) {
            throw new BusinessException("400", "Data de início é obrigatória");
        }

        if (request.expiredAt() == null) {
            throw new BusinessException("400", "Data de expiração é obrigatória");
        }

        if (request.expiredAt().isBefore(request.startedAt())) {
            throw new BusinessException("400",
                    "Data de expiração deve ser posterior à data de início do evento");
        }

        String generateAccessToken = RandomStringUtils.randomAlphanumeric(12);
        String generateAdminToken = RandomStringUtils.random(6, 0, 0, true, true, null, new SecureRandom());

        boolean isPublic = request.isPublic() != null ? request.isPublic() : true;

        Event event = new Event(request.name(), generateAccessToken, request.startedAt(), request.expiredAt(),
                generateAdminToken);
        event.setPublicAlbum(isPublic);

        event = eventRepository.save(event);

        eventToken.markAsUsed(event.getId());
        tokenRepository.save(eventToken);

        return event;

    }

}
