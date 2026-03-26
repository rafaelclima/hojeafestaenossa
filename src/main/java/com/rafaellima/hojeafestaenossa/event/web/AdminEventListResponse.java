package com.rafaellima.hojeafestaenossa.event.web;

import java.util.List;

import org.springframework.data.domain.Page;

public record AdminEventListResponse(
    List<AdminEventItemResponse> content,
    long totalElements,
    int totalPages,
    int number,
    int size
) {
    public static AdminEventListResponse from(Page<AdminEventItemResponse> page) {
        return new AdminEventListResponse(
            page.getContent(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }
}