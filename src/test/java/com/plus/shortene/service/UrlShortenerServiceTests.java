package com.plus.shortene.service;

import com.plus.shortene.postgress.InMemoryPostgresShortUrlRepository;
import com.plus.shortene.redis.InMemoryRedisShortUrlCache;
import com.plus.shortene.request.CreateShortUrlRequest;
import com.plus.shortene.response.ShortUrlResponse;
import com.plus.shortene.snowflake.ShortCodeGenerator;
import com.plus.shortene.url.Sha256UrlHasher;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlShortenerServiceTests {

    @Test
    void returnsExistingShortUrlForDuplicateTrimmedUrl() {
        AtomicInteger sequence = new AtomicInteger();
        ShortCodeGenerator generator =
                () -> "code-" + sequence.incrementAndGet();
        UrlShortenerService service = new UrlShortenerService(
                new InMemoryPostgresShortUrlRepository(),
                new InMemoryRedisShortUrlCache(),
                generator,
                new Sha256UrlHasher()
        );

        ShortUrlResponse first = service.createShortUrl(
                new CreateShortUrlRequest("  https://example.com  ")
        );
        ShortUrlResponse second = service.createShortUrl(
                new CreateShortUrlRequest("https://example.com")
        );

        assertEquals("code-1", first.shortCode());
        assertEquals("https://example.com", first.originalUrl());
        assertEquals(first, second);
        assertEquals(2, sequence.get());
    }
}