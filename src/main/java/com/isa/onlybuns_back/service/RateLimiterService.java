package com.isa.onlybuns_back.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;

    // userId -> lista timestampova zahteva
    private final ConcurrentHashMap<Long, List<LocalDateTime>> userRequestTimes = new ConcurrentHashMap<>();

    public boolean canMakeRequest(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = now.minusMinutes(1);

        // lista zahteva za korisnika
        List<LocalDateTime> requestTimes = userRequestTimes.computeIfAbsent(userId, k -> new ArrayList<>());
        System.out.println("=== GENERAL RATE LIMITER ===");
        System.out.println("User " + userId + " requests before cleanup: " + requestTimes.size());

        // uklanjanje zahteva starijih od minut vremena
        requestTimes.removeIf(time -> time.isBefore(oneMinuteAgo));

        boolean canMake = requestTimes.size() < MAX_REQUESTS_PER_MINUTE;
        System.out.println("User " + userId + " requests after cleanup: " + requestTimes.size() + "/" + MAX_REQUESTS_PER_MINUTE);
        System.out.println("General limiter allows: " + canMake);

        return canMake;
    }

    public void recordRequest(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> requestTimes = userRequestTimes.computeIfAbsent(userId, k -> new ArrayList<>());
        requestTimes.add(now);
        System.out.println("GENERAL: Recorded request for user " + userId + " at " + now + ". Total: " + requestTimes.size());
    }

    public int getRemainingRequests(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = now.minusMinutes(1);

        List<LocalDateTime> requestTimes = userRequestTimes.computeIfAbsent(userId, k -> new ArrayList<>());
        requestTimes.removeIf(time -> time.isBefore(oneMinuteAgo));

        return MAX_REQUESTS_PER_MINUTE - requestTimes.size();
    }

    // vreme kada ce korisnik moci opet da posalje zahtev

    public LocalDateTime getNextAvailableTime(Long userId) {
        List<LocalDateTime> requestTimes = userRequestTimes.get(userId);
        if (requestTimes == null || requestTimes.size() < MAX_REQUESTS_PER_MINUTE) {
            return null; // moze odmah
        }

        // najstariji zahtev + 1 minut
        LocalDateTime oldestRequest = requestTimes.stream()
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        return oldestRequest.plusMinutes(1);
    }

}
