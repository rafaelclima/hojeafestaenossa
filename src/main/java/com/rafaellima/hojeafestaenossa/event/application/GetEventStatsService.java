package com.rafaellima.hojeafestaenossa.event.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.web.EventStatsResponse;
import com.rafaellima.hojeafestaenossa.upload.domain.MediaType;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetEventStatsService {

    private final FindEventByTokenService findEventByTokenService;
    private final UploadRepository uploadRepository;

    public EventStatsResponse execute(String eventToken) {
        Event event = findEventByTokenService.execute(eventToken);
        UUID eventId = event.getId();

        long totalUploads = uploadRepository.countByEventId(eventId);
        long photoCount = uploadRepository.countByEventIdAndMediaType(eventId, MediaType.PHOTO);
        long videoCount = uploadRepository.countByEventIdAndMediaType(eventId, MediaType.VIDEO);

        return new EventStatsResponse(totalUploads, photoCount, videoCount);
    }
}
