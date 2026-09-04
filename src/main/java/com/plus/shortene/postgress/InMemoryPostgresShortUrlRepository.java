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

    private final Map<String, ShortUrl> storage =
            new ConcurrentHashMap<>();

    @Override
    public ShortUrl save(ShortUrl shortUrl) {

        ShortUrl existing =
                storage.putIfAbsent(
                        shortUrl.shortCode(),
                        shortUrl
                );

        if (existing != null) {
            throw new IllegalStateException(
                    "Short code already exists: "
                            + shortUrl.shortCode()
            );
        }

        return shortUrl;
    }

    @Override
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        return Optional.ofNullable(
                storage.get(shortCode)
        );
    }
}
