package com.rafaellima.hojeafestaenossa.event.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.shared.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteEventAdminService {

    private final EventRepository eventRepository;

    public void execute(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("404", "Evento não encontrado");
        }

        eventRepository.deleteById(eventId);
    }
}