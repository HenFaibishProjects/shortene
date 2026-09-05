package com.plus.shortene.snowflake;

// Creates the public value that identifies a shortened URL.
// urlHash prevents duplicate destinations; shortCode identifies the stored URL.
public interface ShortCodeGenerator {

    String generate();
}
