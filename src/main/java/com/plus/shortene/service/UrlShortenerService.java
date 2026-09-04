package com.plus.shortene.service;

import com.plus.shortene.domain.ShortUrl;
import com.plus.shortene.repository.ShortUrlCache;
import com.plus.shortene.repository.ShortUrlRepository;
import com.plus.shortene.request.CreateShortUrlRequest;
import com.plus.shortene.response.ShortUrlResponse;
import com.plus.shortene.snowflake.ShortCodeGenerator;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class UrlShortenerService {

    private static final Duration URL_EXPIRATION = Duration.ofDays(30);

    private final ShortUrlRepository repository;
    private final ShortUrlCache cache;
    private final ShortCodeGenerator shortCodeGenerator;

    public UrlShortenerService(
            ShortUrlRepository repository,
            ShortUrlCache cache,
            ShortCodeGenerator shortCodeGenerator) {

        this.repository = repository;
        this.cache = cache;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request) {

        String shortCode = shortCodeGenerator.generate();

        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plus(URL_EXPIRATION);

        ShortUrl shortUrl = new ShortUrl(
                shortCode,
                request.url(),
                createdAt,
                expiresAt
        );

        repository.save(shortUrl);

        return new ShortUrlResponse(
                shortUrl.shortCode(),
                shortUrl.originalUrl()
        );
    }

    public String resolveUrl(String shortCode) {
        // ניישם בשלב הבא
        return null;
    }
}
