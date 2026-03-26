package com.rafaellima.hojeafestaenossa.event.web;

import java.util.List;

public record StorageByMonthResponse(
    List<MonthData> data
) {
    public record MonthData(
        String month,
        double sizeMB
    ) {}
}