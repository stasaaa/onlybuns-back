package com.isa.onlybuns_back.service;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActiveUserService {
    private final Map<String, Long> userLastSeen = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    public ActiveUserService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void markUserActive(String username) {
        userLastSeen.put(username, Instant.now().toEpochMilli());
    }

    @PostConstruct
    public void initGauge() {
        meterRegistry.gauge("onlybuns.active_users.last_24h", this,
                obj -> obj.countActiveUsers());
    }

    private double countActiveUsers() {
        long now = Instant.now().toEpochMilli();
        long threshold = now - 24 * 60 * 60 * 1000L;
        long count = userLastSeen.values().stream()
                .filter(ts -> ts >= threshold)
                .count();
        return (double) count;
    }
}