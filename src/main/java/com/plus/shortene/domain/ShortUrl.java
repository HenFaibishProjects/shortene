package com.plus.shortene.domain;

import java.time.Instant;

public record ShortUrl(
        // Public value used in the shortened URL.
        String shortCode,
        // Destination returned when the short code is resolved.
        String originalUrl,
        // Internal value used to prevent duplicate long URLs.
        String urlHash,
        // These timestamps define when the short URL was created and when it expires.
        Instant createdAt,
        Instant expiresAt
) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
