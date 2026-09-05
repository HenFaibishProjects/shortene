package com.plus.shortene.url;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// SHA-256 gives the same fixed-length hash for the same URL input.
@Component
public class Sha256UrlHasher implements UrlHasher {

    @Override
    public String hash(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Trimming is intentionally the only normalization because broader
            // URL normalization can change the destination's meaning.
            byte[] hash = digest.digest(url.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}