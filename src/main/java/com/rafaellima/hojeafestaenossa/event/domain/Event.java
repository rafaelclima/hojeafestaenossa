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
@Table(name = "events")
@EqualsAndHashCode(of = "id")
public class Event {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "access_token", nullable = false, length = 120, unique = true)
    private String accessToken;

    @Column(name = "is_public", nullable = false)
    private boolean publicAlbum = true;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "expired_at", nullable = false)
    private Instant expiredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Event() {
    }

    public Event(String name, String accessToken, Instant startedAt, Instant expiredAt) {
        this.name = name;
        this.accessToken = accessToken;
        this.startedAt = startedAt;
        this.expiredAt = expiredAt;
    }

    public UUID getId() {
        return id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean isPublicAlbum() {
        return publicAlbum;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

}
