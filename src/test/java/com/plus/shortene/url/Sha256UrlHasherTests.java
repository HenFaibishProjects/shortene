package com.plus.shortene.url;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Sha256UrlHasherTests {

    private final UrlHasher urlHasher = new Sha256UrlHasher();

    @Test
    void hashesTrimmedUrlUsingSha256() {
        String expectedHash =
                "100680ad546ce6a577f42f52df33b4cfdca756859e664b8d7de329b150d09ce9";

        assertEquals(expectedHash, urlHasher.hash("  https://example.com  "));
        assertEquals(expectedHash, urlHasher.hash("https://example.com"));
    }
}