package com.plus.shortene.postgress;

import com.plus.shortene.domain.ShortUrl;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryPostgresShortUrlRepositoryTest {

    private final InMemoryPostgresShortUrlRepository repository =
            new InMemoryPostgresShortUrlRepository();

    @Test
    void savesNewShortUrl() {
        ShortUrl shortUrl = activeShortUrl("abc123", "hash-1");

        ShortUrl saved = repository.saveOrGetExisting(shortUrl);

        assertSame(shortUrl, saved);
        assertEquals(shortUrl, repository.findByShortCode("abc123").orElseThrow());
    }

    @Test
    void returnsExistingActiveShortUrlForSameUrlHash() {
        ShortUrl first = activeShortUrl("first", "shared-hash");
        ShortUrl second = activeShortUrl("second", "shared-hash");
        repository.saveOrGetExisting(first);

        ShortUrl saved = repository.saveOrGetExisting(second);

        assertSame(first, saved);
        assertTrue(repository.findByShortCode("first").isPresent());
        assertFalse(repository.findByShortCode("second").isPresent());
    }

    @Test
    void replacesExpiredRecordForSameUrlHash() {
        ShortUrl expired = new ShortUrl(
                "expired",
                "https://example.com",
                "shared-hash",
                Instant.now().minusSeconds(60),
                Instant.now().minusSeconds(1)
        );
        ShortUrl replacement = activeShortUrl("replacement", "shared-hash");
        repository.saveOrGetExisting(expired);

        ShortUrl saved = repository.saveOrGetExisting(replacement);

        assertSame(replacement, saved);
        assertFalse(repository.findByShortCode("expired").isPresent());
        assertEquals(
                replacement,
                repository.findByShortCode("replacement").orElseThrow()
        );
    }

    @Test
    void rejectsDuplicateShortCodeForDifferentUrl() {
        ShortUrl first = activeShortUrl("same-code", "first-hash");
        ShortUrl conflicting = activeShortUrl("same-code", "second-hash");
        repository.saveOrGetExisting(first);

        assertThrows(
                IllegalStateException.class,
                () -> repository.saveOrGetExisting(conflicting)
        );
    }

    @Test
    void concurrentSameUrlCreatesSingleActiveRecord() throws Exception {
        int requestCount = 10;
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<ShortUrl>> futures = new ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(requestCount)) {
            for (int index = 0; index < requestCount; index++) {
                String shortCode = "code-" + index;
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return repository.saveOrGetExisting(
                            activeShortUrl(shortCode, "shared-hash")
                    );
                }));
            }

            ready.await();
            start.countDown();

            List<ShortUrl> results = new ArrayList<>();
            for (Future<ShortUrl> future : futures) {
                results.add(future.get());
            }

            assertEquals(1, new HashSet<>(results).size());
            long storedCount = IntStream.range(0, requestCount)
                    .mapToObj(index -> repository.findByShortCode("code-" + index))
                    .filter(java.util.Optional::isPresent)
                    .count();
            assertEquals(1, storedCount);
        }
    }

    private ShortUrl activeShortUrl(String shortCode, String urlHash) {
        Instant createdAt = Instant.now();
        return new ShortUrl(
                shortCode,
                "https://example.com",
                urlHash,
                createdAt,
                createdAt.plusSeconds(60)
        );
    }
}