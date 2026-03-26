package com.rafaellima.hojeafestaenossa.event.application;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.event.repository.EventTokenRepository;
import com.rafaellima.hojeafestaenossa.event.web.EventsByMonthResponse;
import com.rafaellima.hojeafestaenossa.event.web.PlatformStatsResponse;
import com.rafaellima.hojeafestaenossa.event.web.StorageByMonthResponse;
import com.rafaellima.hojeafestaenossa.event.web.TopEventsResponse;
import com.rafaellima.hojeafestaenossa.event.web.UploadsByMonthResponse;
import com.rafaellima.hojeafestaenossa.upload.domain.MediaType;
import com.rafaellima.hojeafestaenossa.upload.repository.UploadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPlatformStatsService {

    private final EventRepository eventRepository;
    private final UploadRepository uploadRepository;
    private final EventTokenRepository eventTokenRepository;

    private static final double BYTES_TO_GB = 1024.0 * 1024.0 * 1024.0;
    private static final int MONTHS_TO_ANALYZE = 12;

    public PlatformStatsResponse getStats() {
        Instant now = Instant.now();

        PlatformStatsResponse.EventsStats eventsStats = buildEventsStats(now);
        PlatformStatsResponse.UploadsStats uploadsStats = buildUploadsStats();
        PlatformStatsResponse.TokensStats tokensStats = buildTokensStats();

        return new PlatformStatsResponse(eventsStats, uploadsStats, tokensStats);
    }

    public EventsByMonthResponse getEventsByMonth() {
        Instant startDate = Instant.now().minus(java.time.Duration.ofDays(365));
        List<Object[]> results = eventRepository.countEventsByMonth(startDate);

        List<EventsByMonthResponse.MonthData> data = results.stream()
                .map(row -> new EventsByMonthResponse.MonthData(
                        (String) row[0],
                        ((Number) row[1]).longValue()))
                .toList();

        return new EventsByMonthResponse(data);
    }

    public UploadsByMonthResponse getUploadsByMonth() {
        Instant startDate = Instant.now().minus(java.time.Duration.ofDays(365));
        List<Object[]> results = uploadRepository.countUploadsByMonth(startDate);

        List<UploadsByMonthResponse.MonthData> data = results.stream()
                .map(row -> new UploadsByMonthResponse.MonthData(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()))
                .toList();

        return new UploadsByMonthResponse(data);
    }

    public TopEventsResponse getTopEvents() {
        List<Object[]> results = uploadRepository.findTop10EventsByUploads();

        List<TopEventsResponse.EventData> data = results.stream()
                .map(row -> new TopEventsResponse.EventData(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()))
                .toList();

        return new TopEventsResponse(data);
    }

    public StorageByMonthResponse getStorageByMonth() {
        Instant startDate = Instant.now().minus(java.time.Duration.ofDays(365));
        List<Object[]> results = uploadRepository.sumStorageByMonth(startDate);

        List<StorageByMonthResponse.MonthData> data = results.stream()
                .map(row -> new StorageByMonthResponse.MonthData(
                        (String) row[0],
                        Math.round(((Number) row[1]).doubleValue() * 100.0) / 100.0))
                .toList();

        return new StorageByMonthResponse(data);
    }

    private PlatformStatsResponse.EventsStats buildEventsStats(Instant now) {
        long total = eventRepository.count();
        long active = eventRepository.countActive(now);
        long expired = eventRepository.countExpired(now);
        long upcoming = eventRepository.countUpcoming(now);

        return new PlatformStatsResponse.EventsStats(total, active, expired, upcoming);
    }

    private PlatformStatsResponse.UploadsStats buildUploadsStats() {
        long total = uploadRepository.count();
        long photos = uploadRepository.countByMediaType(MediaType.PHOTO);
        long videos = uploadRepository.countByMediaType(MediaType.VIDEO);
        long totalBytes = uploadRepository.sumTotalFileSize();
        double totalGB = totalBytes / BYTES_TO_GB;

        return new PlatformStatsResponse.UploadsStats(total, photos, videos, Math.round(totalGB * 1000.0) / 1000.0);
    }

    private PlatformStatsResponse.TokensStats buildTokensStats() {
        long available = eventTokenRepository.countByEventIdIsNull();
        long used = eventTokenRepository.countByEventIdIsNotNull();
        long generated = available + used;

        return new PlatformStatsResponse.TokensStats(generated, used, available);
    }
}