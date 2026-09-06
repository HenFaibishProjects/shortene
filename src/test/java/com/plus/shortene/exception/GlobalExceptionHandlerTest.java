package com.plus.shortene.exception;

import com.plus.shortene.controller.UrlShortenerController;
import com.plus.shortene.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsNotFoundExceptionTo404() {
        ResponseEntity<String> response = handler.handleNotFound(
                new ShortUrlNotFoundException("Short URL not found")
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Short URL not found", response.getBody());
    }

    @Test
    void mapsExpiredExceptionTo410() {
        ResponseEntity<String> response = handler.handleExpired(
                new ShortUrlExpiredException("Short URL expired")
        );

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertEquals("Short URL expired", response.getBody());
    }

    @Test
    void mapsValidationExceptionTo400() throws Exception {
        try (LocalValidatorFactoryBean validator =
                     new LocalValidatorFactoryBean()) {
            validator.afterPropertiesSet();
            MockMvc mockMvc = MockMvcBuilders
                    .standaloneSetup(new UrlShortenerController(
                            mock(UrlShortenerService.class)
                    ))
                    .setControllerAdvice(handler)
                    .setValidator(validator)
                    .build();

            mockMvc.perform(post("/api/urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"url\":\"hello123\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(
                            "URL must be a valid HTTP or HTTPS URL"
                    ));
        }
    }
}