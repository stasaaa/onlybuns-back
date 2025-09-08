package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/posts")
    public Map<String, Long> getPostStats(@RequestParam String range) {
        return analyticsService.countPostsByRange(range);
    }

    @GetMapping("/comments")
    public Map<String, Long> getCommentStats(@RequestParam String range) {
        return analyticsService.countCommentsByRange(range);
    }

    @GetMapping("/user-activity")
    public Map<String, Double> getUserActivityStats() {
        return analyticsService.calculateUserActivityStats();
    }
}
