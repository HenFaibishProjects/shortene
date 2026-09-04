package com.plus.shortene.controller;

import com.plus.shortene.request.CreateShortUrlRequest;
import com.plus.shortene.response.ShortUrlResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlShortenerController {

    @PostMapping("/api/urls")
    public ShortUrlResponse createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request) {

        return null;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode) {

        return null;
    }
}
