package com.plus.shortene.snowflake;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class LocalShortCodeGenerator
        implements ShortCodeGenerator {

    private final AtomicLong sequence =
            new AtomicLong(System.currentTimeMillis());

    @Override
    public String generate() {
        return String.valueOf(sequence.incrementAndGet());
    }
}
