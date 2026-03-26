package com.rafaellima.hojeafestaenossa.event.web;

import java.util.List;

public record EventsByMonthResponse(
    List<MonthData> data
) {
    public record MonthData(
        String month,
        long count
    ) {}
}