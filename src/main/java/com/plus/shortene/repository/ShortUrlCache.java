package com.plus.shortene.repository;

import java.time.Duration;
import java.util.Optional;

// Caches shortCode -> originalUrl lookups for the resolve flow.
// It is not used for creation deduplication; PostgreSQL remains the source of truth.
public interface ShortUrlCache {

    // Returns the cached destination when a live entry exists.
    Optional<String> get(String shortCode);

    // Caches a destination with a TTL so it cannot outlive its intended lifetime.
    void put(
            String shortCode,
            String originalUrl,
            Duration ttl
    );
}
