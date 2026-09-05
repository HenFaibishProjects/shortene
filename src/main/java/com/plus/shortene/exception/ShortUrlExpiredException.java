package com.plus.shortene.exception;

public class ShortUrlExpiredException extends RuntimeException {

    public ShortUrlExpiredException(String message) {
        super(message);
    }
}
