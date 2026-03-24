package com.rafaellima.hojeafestaenossa.event.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rafaellima.hojeafestaenossa.event.domain.EventToken;

public interface EventTokenRepository extends JpaRepository<EventToken, UUID> {

    Optional<EventToken> findByToken(String token);

    @Query("SELECT t FROM EventToken t ORDER BY t.createdAt DESC")
    List<EventToken> findAllOrderByCreatedAtDesc();
}