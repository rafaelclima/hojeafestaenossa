package com.rafaellima.hojeafestaenossa.event.web;

import java.time.Instant;
import java.util.List;

public record ManifestExport(
    String eventName,
    String eventToken,
    Instant exportedAt,
    int totalFiles,
    Summary summary,
    List<FileEntry> files
) {
    public record Summary(
        int photos,
        int videos,
        double totalSizeMB
    ) {}

    public record FileEntry(
        String fileName,
        String type,
        String message,
        Instant createdAt,
        long fileSizeBytes
    ) {}
}