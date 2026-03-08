package com.rafaellima.hojeafestaenossa.upload.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.rafaellima.hojeafestaenossa.upload.domain.MediaType;
import com.rafaellima.hojeafestaenossa.upload.domain.Upload;

public interface UploadRepository extends JpaRepository<Upload, UUID> {

    Page<Upload> findByEventIdAndVisibleTrueOrderByCreatedAtDesc(UUID eventId, Pageable pageable);

    Page<Upload> findAllByEventIdOrderByCreatedAtDesc(UUID eventId, Pageable pageable);

    void deleteById(UUID uploadId);

    long countByEventId(UUID eventId);

    long countByEventIdAndMediaType(UUID eventId, MediaType mediaType);

}
