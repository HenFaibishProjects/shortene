package com.plus.shortene.validation;

import com.plus.shortene.request.CreateShortUrlRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("HttpUrlsUsage")
class CreateShortUrlRequestValidationTests {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void leavesNullValidationToNotBlank() {
        assertTrue(new ValidHttpUrlValidator().isValid(null, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://google.com",
            "http://example.com",
            "https://www.linkedin.com/jobs/search?x=1",
            "https://example.com/path?x=1&y=2",
            "https://sub.example.com"
    })
    void acceptsValidHttpUrls(String url) {
        assertTrue(validator.validate(new CreateShortUrlRequest(url)).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "hello123",
            "google.com",
            "ftp://example.com",
            "https://",
            "",
            "   ",
            "www.example.com",
            "mailto:test@example.com",
            "javascript:alert(1)"
    })
    void rejectsInvalidHttpUrls(String url) {
        assertFalse(validator.validate(new CreateShortUrlRequest(url)).isEmpty());
    }

    @Test
    void rejectsNullUrl() {
        assertFalse(validator.validate(new CreateShortUrlRequest(null)).isEmpty());
    }
}