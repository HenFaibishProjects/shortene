package com.plus.shortene.service;

import com.plus.shortene.domain.ShortUrl;
import com.plus.shortene.exception.ShortUrlExpiredException;
import com.plus.shortene.exception.ShortUrlNotFoundException;
import com.plus.shortene.repository.ShortUrlCache;
import com.plus.shortene.repository.ShortUrlRepository;
import com.plus.shortene.request.CreateShortUrlRequest;
import com.plus.shortene.response.ShortUrlResponse;
import com.plus.shortene.snowflake.ShortCodeGenerator;
import com.plus.shortene.url.UrlHasher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/*
 * CREATE:
 * long URL -> hash for deduplication -> short code -> repository -> response.
 *
 * RESOLVE:
 * short code -> Redis cache -> PostgreSQL on cache miss -> expiration check -> cache -> redirect.
 */
@Service
public class UrlShortenerService {

    private static final Duration URL_EXPIRATION = Duration.ofDays(30);

    private final ShortUrlRepository repository;
    private final ShortUrlCache cache;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlHasher urlHasher;

    public UrlShortenerService(
            ShortUrlRepository repository,
            ShortUrlCache cache,
            ShortCodeGenerator shortCodeGenerator,
            UrlHasher urlHasher) {

        this.repository = repository;
        this.cache = cache;
        this.shortCodeGenerator = shortCodeGenerator;
        this.urlHasher = urlHasher;
    }

    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request) {

        // Trim surrounding whitespace without changing the URL's meaning.
        String originalUrl = request.url().trim();

        // The stable hash lets the repository detect the same long URL.
        // It is used for deduplication and is not the public short code.
        String urlHash = urlHasher.hash(originalUrl);

        // The short code identifies the shortened URL. Production would use
        // a distributed Snowflake-style generator for this value.
        String shortCode = shortCodeGenerator.generate();

        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plus(URL_EXPIRATION);

        // Keep the public code, destination, deduplication hash, and lifetime together.
        ShortUrl shortUrl = new ShortUrl(
                shortCode,
                originalUrl,
                urlHash,
                createdAt,
                expiresAt
        );

        // Deduplication and insertion happen atomically in the repository.
        // The result may be this new record or an existing active record.
        ShortUrl saved = repository.saveOrGetExisting(shortUrl);

        // Always respond from the repository result so duplicates reuse its short code.
        return new ShortUrlResponse(
                saved.shortCode(),
                saved.originalUrl()
        );
    }

    public String resolveUrl(String shortCode) {

        // First try Redis.
        Optional<String> cachedUrl = cache.get(shortCode);

        if (cachedUrl.isPresent()) {
            return cachedUrl.get();
        }

        // Cache miss: read from PostgreSQL.
        ShortUrl shortUrl = repository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ShortUrlNotFoundException(
                                "Short URL not found: " + shortCode
                        )
                );

        // Expired URLs should no longer be resolved.
        if (shortUrl.isExpired()) {
            throw new ShortUrlExpiredException(
                    "Short URL expired: " + shortCode
            );
        }

        // Cache only until the URL itself expires.
        Duration ttl = Duration.between(
                Instant.now(),
                shortUrl.expiresAt()
        );

        cache.put(
                shortCode,
                shortUrl.originalUrl(),
                ttl
        );

        return shortUrl.originalUrl();
    }
}
