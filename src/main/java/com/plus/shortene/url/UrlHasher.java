package com.plus.shortene.url;

// Produces an internal deduplication key from a long URL.
// The hash is not the public short code.
// This hash is not shown to the user.
// It is only used internally to detect duplicate long URLs.
public interface UrlHasher {

    String hash(String url);
}