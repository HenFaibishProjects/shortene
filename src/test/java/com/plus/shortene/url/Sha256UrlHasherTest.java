package com.plus.shortene.url;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Sha256UrlHasherTest {

    private final UrlHasher urlHasher = new Sha256UrlHasher();

    @Test
    void sameUrlProducesSameHash() {
        String url = "https://example.com/path?x=1";

        assertEquals(urlHasher.hash(url), urlHasher.hash(url));
    }

    @Test
    void differentUrlsProduceDifferentHashes() {
        assertNotEquals(
                urlHasher.hash("https://example.com/first"),
                urlHasher.hash("https://example.com/second")
        );
    }

    @Test
    void hashHasExpectedSha256HexLength() {
        String hash = urlHasher.hash("https://example.com");

        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void hasherDoesNotPerformUrlValidation() {
        String hash = assertDoesNotThrow(() -> urlHasher.hash("hello123"));

        assertEquals(64, hash.length());
    }
}