package com.isa.onlybuns_back.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Profile("loadbalancer")
public class LoadBalancerController {

    private final List<String> backendServers = List.of(
            "http://localhost:8081",
            "http://localhost:8082"
    );

    private final AtomicInteger currentIndex = new AtomicInteger(0);

    private final RestTemplate restTemplate;

    public LoadBalancerController(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build();
    }

    @RequestMapping("/**")
    public ResponseEntity<?> proxyRequest(HttpServletRequest request,
                                          HttpMethod method,
                                          @RequestBody(required = false) String body) {
        int attempts = 0;
        int maxAttempts = backendServers.size();

        while (attempts < maxAttempts) {
            String targetServer = getNextServer();

            String forwardUrl = targetServer + getRequestUri(request);

            try {
                HttpHeaders headers = new HttpHeaders();
                Collections.list(request.getHeaderNames())
                        .forEach(headerName -> headers.add(headerName, request.getHeader(headerName)));

                HttpEntity<String> entity = new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        forwardUrl,
                        method,
                        entity,
                        String.class);

                return ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .body(response.getBody());
            } catch (ResourceAccessException | HttpClientErrorException | HttpServerErrorException ex) {
                // server nije dostupan ili greška - pokušaj sledeći
                attempts++;
                System.out.println("Server " + targetServer + " nije dostupan. Pokušavam sledeći...");
            }
        }

        // ako nijedan server nije dostupan
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body("Svi backend serveri su nedostupni.");
    }

    private String getNextServer() {
        int idx = currentIndex.getAndUpdate(i -> (i + 1) % backendServers.size());
        return backendServers.get(idx);
    }

    private String getRequestUri(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String uri = request.getRequestURI();
        return queryString == null ? uri : uri + "?" + queryString;
    }
}
