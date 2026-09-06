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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private ShortUrlRepository repository;

    @Mock
    private ShortUrlCache cache;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @Mock
    private UrlHasher urlHasher;

    @Captor
    private ArgumentCaptor<ShortUrl> shortUrlCaptor;

    @InjectMocks
    private UrlShortenerService service;

    @Test
    void createsNewShortUrl() {
        CreateShortUrlRequest request =
                new CreateShortUrlRequest("  https://example.com/path  ");
        when(urlHasher.hash("https://example.com/path"))
                .thenReturn("known-hash");
        when(shortCodeGenerator.generate()).thenReturn("abc123");
        when(repository.saveOrGetExisting(any(ShortUrl.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrlResponse response = service.createShortUrl(request);

        assertEquals("abc123", response.shortCode());
        assertEquals("https://example.com/path", response.originalUrl());
        verify(urlHasher).hash("https://example.com/path");
        verify(shortCodeGenerator, times(1)).generate();
        verify(repository).saveOrGetExisting(shortUrlCaptor.capture());

        ShortUrl candidate = shortUrlCaptor.getValue();
        assertEquals("abc123", candidate.shortCode());
        assertEquals("https://example.com/path", candidate.originalUrl());
        assertEquals("known-hash", candidate.urlHash());
        assertNotNull(candidate.createdAt());
        assertNotNull(candidate.expiresAt());
        assertTrue(candidate.expiresAt().isAfter(candidate.createdAt()));
    }

    @Test
    void returnsExistingShortUrlWhenRepositoryDeduplicates() {
        CreateShortUrlRequest request =
                new CreateShortUrlRequest("https://example.com");
        Instant createdAt = Instant.now();
        ShortUrl existing = new ShortUrl(
                "existing-code",
                "https://example.com",
                "known-hash",
                createdAt,
                createdAt.plusSeconds(60)
        );
        when(urlHasher.hash("https://example.com")).thenReturn("known-hash");
        when(shortCodeGenerator.generate()).thenReturn("candidate-code");
        when(repository.saveOrGetExisting(any(ShortUrl.class)))
                .thenReturn(existing);

        ShortUrlResponse response = service.createShortUrl(request);

        assertEquals("existing-code", response.shortCode());
        assertEquals(existing.originalUrl(), response.originalUrl());
        assertNotEquals("candidate-code", response.shortCode());
    }

    @Test
    void returnsCachedUrlWithoutCallingRepository() {
        String shortCode = "abc123";
        String originalUrl = "https://example.com";
        when(cache.get(shortCode)).thenReturn(Optional.of(originalUrl));

        String resolvedUrl = service.resolveUrl(shortCode);

        assertEquals(originalUrl, resolvedUrl);
        verify(cache).get(shortCode);
        verify(repository, never()).findByShortCode(anyString());
        verify(cache, never()).put(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void loadsFromRepositoryAndCachesOnCacheMiss() {
        String shortCode = "abc123";
        String originalUrl = "https://example.com";
        Instant expiresAt = Instant.now().plusSeconds(60);
        ShortUrl stored = new ShortUrl(
                shortCode,
                originalUrl,
                "known-hash",
                Instant.now(),
                expiresAt
        );
        when(cache.get(shortCode)).thenReturn(Optional.empty());
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(stored));
        Instant beforeResolve = Instant.now();

        String resolvedUrl = service.resolveUrl(shortCode);

        assertEquals(originalUrl, resolvedUrl);
        verify(repository).findByShortCode(shortCode);

        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(cache).put(
                org.mockito.ArgumentMatchers.eq(shortCode),
                org.mockito.ArgumentMatchers.eq(originalUrl),
                ttlCaptor.capture()
        );

        Duration ttl = ttlCaptor.getValue();
        Duration remainingAtStart = Duration.between(beforeResolve, expiresAt);
        assertTrue(ttl.isPositive());
        assertTrue(ttl.compareTo(remainingAtStart) <= 0);
    }

    @Test
    void throwsNotFoundWhenShortCodeDoesNotExist() {
        String shortCode = "missing";
        when(cache.get(shortCode)).thenReturn(Optional.empty());
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        assertThrows(
                ShortUrlNotFoundException.class,
                () -> service.resolveUrl(shortCode)
        );

        verify(cache, never()).put(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void throwsExpiredWhenShortUrlIsExpired() {
        String shortCode = "expired";
        ShortUrl expired = new ShortUrl(
                shortCode,
                "https://example.com",
                "known-hash",
                Instant.now().minusSeconds(60),
                Instant.now().minusSeconds(1)
        );
        when(cache.get(shortCode)).thenReturn(Optional.empty());
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(expired));

        assertThrows(
                ShortUrlExpiredException.class,
                () -> service.resolveUrl(shortCode)
        );

        verify(cache, never()).put(anyString(), anyString(), any(Duration.class));
    }

}