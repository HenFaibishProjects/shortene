package com.plus.shortene.redis;

import com.plus.shortene.repository.ShortUrlCache;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRedisShortUrlCache implements ShortUrlCache {

    private final Map<String, CacheEntry> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<String> get(String shortCode) {
        CacheEntry entry = storage.get(shortCode);

        if (entry == null) {
            return Optional.empty();
        }

        if (Instant.now().isAfter(entry.expiresAt())) {
            storage.remove(shortCode, entry);
            return Optional.empty();
        }

        return Optional.of(entry.originalUrl());
    }

    @Override
    public void put(
            String shortCode,
            String originalUrl,
            Duration ttl
    ) {
        Instant expiresAt = Instant.now().plus(ttl);
        storage.put(shortCode, new CacheEntry(originalUrl, expiresAt));
    }

    private record CacheEntry(
            String originalUrl,
            Instant expiresAt
    ) {
    }
}