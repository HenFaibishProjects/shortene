package com.plus.shortene.response;

public record ShortUrlResponse(
        String shortCode,
        String originalUrl
) {
}
