package com.plus.shortene.postgress;

import com.plus.shortene.domain.ShortUrl;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryPostgresShortUrlRepositoryTests {

    private final InMemoryPostgresShortUrlRepository repository =
            new InMemoryPostgresShortUrlRepository();

    @Test
    void returnsActiveRecordWithSameUrlHash() {
        ShortUrl existing = activeShortUrl("first", "hash");
        ShortUrl duplicate = activeShortUrl("second", "hash");

        repository.saveOrGetExisting(existing);
        ShortUrl saved = repository.saveOrGetExisting(duplicate);

        assertSame(existing, saved);
        assertTrue(repository.findByShortCode("first").isPresent());
        assertFalse(repository.findByShortCode("second").isPresent());
    }

    @Test
    void replacesExpiredRecordWithSameUrlHash() {
        ShortUrl expired = new ShortUrl(
                "expired",
                "https://example.com",
                "hash",
                Instant.now().minusSeconds(60),
                Instant.now().minusSeconds(1)
        );
        ShortUrl replacement = activeShortUrl("replacement", "hash");

        repository.saveOrGetExisting(expired);
        ShortUrl saved = repository.saveOrGetExisting(replacement);

        assertSame(replacement, saved);
        assertFalse(repository.findByShortCode("expired").isPresent());
        assertEquals(replacement, repository.findByShortCode("replacement").orElseThrow());
    }

    @Test
    void rejectsDuplicateShortCodeForDifferentUrlHash() {
        ShortUrl first = activeShortUrl("same-code", "first-hash");
        ShortUrl conflicting = activeShortUrl("same-code", "second-hash");

        repository.saveOrGetExisting(first);

        assertThrows(
                IllegalStateException.class,
                () -> repository.saveOrGetExisting(conflicting)
        );
        assertEquals(first, repository.findByShortCode("same-code").orElseThrow());
    }

    @Test
    void atomicallyDeduplicatesConcurrentRequests() throws Exception {
        int requestCount = 20;
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

            String savedShortCode = results.getFirst().shortCode();
            assertTrue(results.stream()
                    .allMatch(result -> result.shortCode().equals(savedShortCode)));

            long savedRecordCount = java.util.stream.IntStream.range(0, requestCount)
                    .mapToObj(index -> repository.findByShortCode("code-" + index))
                    .filter(java.util.Optional::isPresent)
                    .count();
            assertEquals(1, savedRecordCount);
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