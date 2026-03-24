package com.rafaellima.hojeafestaenossa.shared.util;

import java.text.Normalizer;

public final class SlugUtils {

    private SlugUtils() {
    }

    public static String slugify(String text) {
        return slugify(text, 30);
    }

    public static String slugify(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        String lowercase = withoutAccents.toLowerCase();
        String alphanumeric = lowercase.replaceAll("[^a-z0-9\\s-]", "");
        String hyphenated = alphanumeric.replaceAll("[\\s_]+", "-");
        String trimmed = hyphenated.replaceAll("^-+|-+$", "");

        if (trimmed.length() > maxLength) {
            trimmed = trimmed.substring(0, maxLength);
            int lastHyphen = trimmed.lastIndexOf('-');
            if (lastHyphen > 0) {
                trimmed = trimmed.substring(0, lastHyphen);
            }
        }

        return trimmed.replaceAll("-{2,}", "-").replaceAll("^-+|-+$", "");
    }
}