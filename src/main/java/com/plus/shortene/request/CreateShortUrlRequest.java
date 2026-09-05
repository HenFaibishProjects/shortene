package com.plus.shortene.request;

import com.plus.shortene.validation.ValidHttpUrl;
import jakarta.validation.constraints.NotBlank;

public record CreateShortUrlRequest(

        @NotBlank
        @ValidHttpUrl
        String url

) {
}
