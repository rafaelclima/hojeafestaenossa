package com.rafaellima.hojeafestaenossa.event.web;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rafaellima.hojeafestaenossa.event.application.DeleteEventAdminService;
import com.rafaellima.hojeafestaenossa.event.application.ExportEventMediaService;
import com.rafaellima.hojeafestaenossa.event.application.GetPlatformStatsService;
import com.rafaellima.hojeafestaenossa.event.application.ListEventsAdminService;
import com.rafaellima.hojeafestaenossa.event.domain.Event;
import com.rafaellima.hojeafestaenossa.event.repository.EventRepository;
import com.rafaellima.hojeafestaenossa.shared.exception.NotFoundException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final ListEventsAdminService listEventsAdminService;
    private final DeleteEventAdminService deleteEventAdminService;
    private final GetPlatformStatsService getPlatformStatsService;
    private final ExportEventMediaService exportEventMediaService;
    private final EventRepository eventRepository;

    @GetMapping
    public AdminEventListResponse listEvents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Instant fromInstant = parseInstant(from);
        Instant toInstant = parseInstant(to);

        return listEventsAdminService.execute(status, fromInstant, toInstant, page, size);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        deleteEventAdminService.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/export")
    public void exportEventMedia(
            @PathVariable UUID id,
            HttpServletResponse response) throws IOException {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("404", "Evento não encontrado"));

        String zipFileName = exportEventMediaService.generateZipFileName(event);

        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"");

        exportEventMediaService.exportToZip(id, response.getOutputStream());
    }

    @GetMapping("/stats")
    public PlatformStatsResponse getStats() {
        return getPlatformStatsService.getStats();
    }

    @GetMapping("/stats/events-by-month")
    public EventsByMonthResponse getEventsByMonth() {
        return getPlatformStatsService.getEventsByMonth();
    }

    @GetMapping("/stats/uploads-by-month")
    public UploadsByMonthResponse getUploadsByMonth() {
        return getPlatformStatsService.getUploadsByMonth();
    }

    @GetMapping("/stats/top-events")
    public TopEventsResponse getTopEvents() {
        return getPlatformStatsService.getTopEvents();
    }

    @GetMapping("/stats/storage-by-month")
    public StorageByMonthResponse getStorageByMonth() {
        return getPlatformStatsService.getStorageByMonth();
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Instant.parse(value);
    }
}