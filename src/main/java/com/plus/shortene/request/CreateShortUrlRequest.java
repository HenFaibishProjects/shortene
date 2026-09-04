package com.plus.shortene.request;

import jakarta.validation.constraints.NotBlank;

public record CreateShortUrlRequest(

        @NotBlank
        String url

) {
}
