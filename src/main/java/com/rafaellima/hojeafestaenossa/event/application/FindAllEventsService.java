package com.rafaellima.hojeafestaenossa.event.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindAllEventsService {

    private final EventRepository eventRepository;

    public Page<Event> execute(Pageable pageable) {
        return eventRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

}
