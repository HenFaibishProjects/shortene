package com.plus.shortene.postgress;

import com.plus.shortene.domain.ShortUrl;
import com.plus.shortene.repository.ShortUrlRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPostgresShortUrlRepository
        implements ShortUrlRepository {

    // Primary lookup index used when resolving a public short code.
    private final Map<String, ShortUrl> storageByShortCode =
            new ConcurrentHashMap<>();

    // Secondary index used to detect an existing record for the same long URL.
    private final Map<String, ShortUrl> storageByUrlHash =
            new ConcurrentHashMap<>();

    // Two requests for the same URL may arrive at the same time.
    // Synchronizing the whole operation guarantees only one active record is created.
    @Override
    public synchronized ShortUrl saveOrGetExisting(ShortUrl shortUrl) {
        ShortUrl existingByUrlHash = storageByUrlHash.get(shortUrl.urlHash());

        // An active hash match wins, so the new candidate is not stored.
        if (existingByUrlHash != null && !existingByUrlHash.isExpired()) {
            return existingByUrlHash;
        }

        ShortUrl existingByShortCode =
                storageByShortCode.get(shortUrl.shortCode());

        if (existingByShortCode != null
                && existingByShortCode != existingByUrlHash) {
            throw new IllegalStateException(
                    "Short code already exists: "
                            + shortUrl.shortCode()
            );
        }

        // An expired hash match no longer blocks a fresh short URL.
        if (existingByUrlHash != null) {
            storageByShortCode.remove(
                    existingByUrlHash.shortCode(),
                    existingByUrlHash
            );
            storageByUrlHash.remove(
                    existingByUrlHash.urlHash(),
                    existingByUrlHash
            );
        }

        // PostgreSQL would perform these uniqueness checks and insertion atomically
        // with constraints on short_code and url_hash plus conflict handling.
        storageByShortCode.put(shortUrl.shortCode(), shortUrl);
        storageByUrlHash.put(shortUrl.urlHash(), shortUrl);

        return shortUrl;
    }

    @Override
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        return Optional.ofNullable(
                storageByShortCode.get(shortCode)
        );
    }
}
