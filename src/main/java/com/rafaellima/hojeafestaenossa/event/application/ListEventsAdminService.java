package com.rafaellima.hojeafestaenossa.event.application;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.event.repository.EventTokenRepository;
import com.rafaellima.hojeafestaenossa.event.web.AdminEventItemResponse;
import com.rafaellima.hojeafestaenossa.event.web.AdminEventListResponse;
import com.rafaellima.hojeafestaenossa.upload.domain.MediaType;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListEventsAdminService {

    private final EventRepository eventRepository;
    private final UploadRepository uploadRepository;
    private final EventTokenRepository eventTokenRepository;

    public AdminEventListResponse execute(String status, Instant from, Instant to, int page, int size) {
        Instant now = Instant.now();
        Pageable pageable = PageRequest.of(page, size);

        Page<Event> events = eventRepository.findAllByOrderByCreatedAtDesc(pageable);

        Page<AdminEventItemResponse> responsePage = events.map(event -> mapToResponse(event, now));

        return AdminEventListResponse.from(responsePage);
    }

    private AdminEventItemResponse mapToResponse(Event event, Instant now) {
        String eventStatus = calculateStatus(event, now);

        long totalUploads = uploadRepository.countByEventId(event.getId());
        long photoCount = uploadRepository.countByEventIdAndMediaType(event.getId(), MediaType.PHOTO);
        long videoCount = uploadRepository.countByEventIdAndMediaType(event.getId(), MediaType.VIDEO);

        long storageBytes = uploadRepository.sumFileSizeByEventId(event.getId());
        double storageMB = storageBytes / (1024.0 * 1024.0);

        AdminEventItemResponse.UploadsSummary uploads = new AdminEventItemResponse.UploadsSummary(
                totalUploads, photoCount, videoCount);

        return new AdminEventItemResponse(
                event.getId(),
                event.getName(),
                event.getAccessToken(),
                event.getStartedAt(),
                event.getExpiredAt(),
                event.getCreatedAt(),
                eventStatus,
                uploads,
                Math.round(storageMB * 100.0) / 100.0);
    }

    private String calculateStatus(Event event, Instant now) {
        if (now.isBefore(event.getStartedAt())) {
            return "upcoming";
        } else if (now.isAfter(event.getExpiredAt())) {
            return "expired";
        } else {
            return "active";
        }
    }
}