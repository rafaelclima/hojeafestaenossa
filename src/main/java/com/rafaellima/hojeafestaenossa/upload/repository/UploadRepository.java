package com.rafaellima.hojeafestaenossa.upload.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rafaellima.hojeafestaenossa.upload.domain.Upload;

public interface UploadRepository extends JpaRepository<Upload, UUID> {

    List<Upload> findTop50ByEventIdAndVisibleTrueOrderByCreatedAtDesc(UUID eventId);

    long countbyEventId(UUID eventId);

}
