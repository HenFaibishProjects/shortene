package com.plus.shortene.controller;

import com.plus.shortene.request.CreateShortUrlRequest;
import com.plus.shortene.response.ShortUrlResponse;
import com.plus.shortene.service.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    public UrlShortenerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    // Entry point for creating a short URL from the original long URL.
    // The controller validates the request and delegates the work to the service.
    @PostMapping("/api/urls")
    public ShortUrlResponse createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request) {

        return urlShortenerService.createShortUrl(request);
    }

    // Planned entry point for resolving a short code and redirecting the client.
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode) {

        // TODO: Implement the redirect flow after URL resolution is available.
        return null;
    }
}
