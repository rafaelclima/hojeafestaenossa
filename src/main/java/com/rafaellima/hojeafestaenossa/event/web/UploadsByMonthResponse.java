package com.rafaellima.hojeafestaenossa.event.web;

import java.util.List;

public record UploadsByMonthResponse(
    List<MonthData> data
) {
    public record MonthData(
        String month,
        long photos,
        long videos,
        long total
    ) {}
}