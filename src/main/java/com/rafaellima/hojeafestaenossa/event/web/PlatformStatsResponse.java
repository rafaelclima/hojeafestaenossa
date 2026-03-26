package com.rafaellima.hojeafestaenossa.event.web;

public record PlatformStatsResponse(
    EventsStats events,
    UploadsStats uploads,
    TokensStats tokens
) {
    public record EventsStats(
        long total,
        long active,
        long expired,
        long upcoming
    ) {}

    public record UploadsStats(
        long total,
        long photos,
        long videos,
        double totalSizeGB
    ) {}

    public record TokensStats(
        long generated,
        long used,
        long available
    ) {}
}