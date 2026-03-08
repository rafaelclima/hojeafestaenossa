package com.rafaellima.hojeafestaenossa.upload.web;

import jakarta.validation.constraints.NotNull;

public record VisibilityRequest(
        @NotNull Boolean visible) {
}
