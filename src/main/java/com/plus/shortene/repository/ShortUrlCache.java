package com.plus.shortene.repository;

import java.time.Duration;
import java.util.Optional;

public interface ShortUrlCache {

    Optional<String> get(String shortCode);

    void put(
            String shortCode,
            String originalUrl,
            Duration ttl
    );
}
