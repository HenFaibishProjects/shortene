package com.plus.shortene.repository;

import com.plus.shortene.domain.ShortUrl;

import java.util.Optional;

public interface ShortUrlRepository {

    // Tries to save a short URL in one atomic operation.
    // If the same URL already has an active record, that record is returned instead.
    // This models PostgreSQL UNIQUE constraints and INSERT conflict handling.
    ShortUrl saveOrGetExisting(ShortUrl shortUrl);

    // Finds the record needed to resolve a public short code.
    Optional<ShortUrl> findByShortCode(String shortCode);
}
