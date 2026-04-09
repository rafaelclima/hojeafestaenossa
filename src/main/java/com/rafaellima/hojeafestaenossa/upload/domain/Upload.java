package com.rafaellima.hojeafestaenossa.upload.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "uploads")
@EqualsAndHashCode(of = "id")
public class Upload {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private MediaType mediaType;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_visible", nullable = false)
    private Boolean visible = false;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Upload() {
    }

    public Upload(
            UUID eventId,
            MediaType mediaType,
            String storageKey,
            String originalName,
            long fileSize,
            String message,
            String url,
            String thumbnailUrl) {
        this.eventId = eventId;
        this.mediaType = mediaType;
        this.storageKey = storageKey;
        this.originalName = originalName;
        this.fileSize = fileSize;
        this.message = message;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.visible = false;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisibility(boolean visible) {
        this.visible = visible;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

}
