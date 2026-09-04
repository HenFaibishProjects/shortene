package com.plus.shortene.domain;

import java.time.Instant;

public record ShortUrl(
        String shortCode,
        String originalUrl,
        Instant createdAt,
        Instant expiresAt
) {
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
