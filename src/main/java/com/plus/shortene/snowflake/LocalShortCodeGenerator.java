package com.plus.shortene.snowflake;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

// Local simulation of a production distributed generator such as Snowflake.
// It generates short codes only; URL hashes solve the separate deduplication problem.
@Component
public class LocalShortCodeGenerator
        implements ShortCodeGenerator {

    // AtomicLong keeps locally generated values unique across concurrent requests.
    private final AtomicLong sequence =
            new AtomicLong(System.currentTimeMillis());

    @Override
    public String generate() {
        return String.valueOf(sequence.incrementAndGet());
    }
}
