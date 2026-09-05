package com.plus.shortene.exception;

import com.plus.shortene.controller.UrlShortenerController;
import com.plus.shortene.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTests {

    @Test
    void returnsActualValidationMessage() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        try {
            MockMvc mockMvc = MockMvcBuilders
                    .standaloneSetup(new UrlShortenerController(
                            mock(UrlShortenerService.class)
                    ))
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .setValidator(validator)
                    .build();

            mockMvc.perform(post("/api/urls")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"url\":\"hello123\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(
                            "URL must be a valid HTTP or HTTPS URL"
                    ));
        } finally {
            validator.close();
        }
    }
}