package com.rafaellima.hojeafestaenossa.event.web;

public record EventStatsResponse(
        long totalUploads,
        long photoCount,
        long videoCount) {
}
