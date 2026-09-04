package com.plus.shortene.repository;

import com.plus.shortene.domain.ShortUrl;

import java.util.Optional;

public interface ShortUrlRepository {

    ShortUrl save(ShortUrl shortUrl);

    Optional<ShortUrl> findByShortCode(String shortCode);
}
