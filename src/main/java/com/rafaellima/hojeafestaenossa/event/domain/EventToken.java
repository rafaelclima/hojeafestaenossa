package com.rafaellima.hojeafestaenossa.event.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "event_tokens")
@EqualsAndHashCode(of = "id")
public class EventToken {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String token;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected EventToken() {
    }

    public EventToken(String token) {
        this.token = token;
    }

    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isUsed() {
        return eventId != null;
    }

    public void markAsUsed(UUID eventId) {
        this.eventId = eventId;
        this.usedAt = Instant.now();
    }
}