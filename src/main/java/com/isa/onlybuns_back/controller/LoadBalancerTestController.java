package com.isa.onlybuns_back.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("backend")
public class LoadBalancerTestController {

    @GetMapping("/hello")
    public String hello(HttpServletRequest request) {
        return "Hello from backend instance on port: " + request.getLocalPort();
    }
}
