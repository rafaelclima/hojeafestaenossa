package com.rafaellima.hojeafestaenossa.event.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rafaellima.hojeafestaenossa.event.domain.Event;

public interface EventRepository extends JpaRepository<Event, UUID> {

    Optional<Event> findByAccessToken(String accessToken);

    Optional<Event> findBySlug(String slug);

}
