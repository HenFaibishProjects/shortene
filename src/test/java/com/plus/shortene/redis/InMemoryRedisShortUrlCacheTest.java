package com.plus.shortene.redis;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRedisShortUrlCacheTest {

    private final InMemoryRedisShortUrlCache cache =
            new InMemoryRedisShortUrlCache();

    @Test
    void returnsEmptyForMissingKey() {
        assertTrue(cache.get("missing").isEmpty());
    }

    @Test
    void storesAndReturnsValue() {
        cache.put(
                "abc123",
                "https://example.com",
                Duration.ofMinutes(1)
        );

        assertEquals(
                "https://example.com",
                cache.get("abc123").orElseThrow()
        );
    }

    @Test
    void returnsEmptyAfterExpiration() throws InterruptedException {
        cache.put(
                "abc123",
                "https://example.com",
                Duration.ofMillis(1)
        );

        Thread.sleep(10);

        assertTrue(cache.get("abc123").isEmpty());
    }

    @Test
    void expiredEntryIsRemovedLazily() {
        cache.put(
                "abc123",
                "https://example.com",
                Duration.ofMillis(-1)
        );

        assertTrue(cache.get("abc123").isEmpty());
        assertTrue(cache.get("abc123").isEmpty());
    }
}