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

    // cache poslednjeg cleanup vremena po korisniku
    private final ConcurrentHashMap<Long, LocalDateTime> lastCleanupTime = new ConcurrentHashMap<>();

    private synchronized void cleanupIfNeeded(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastCleanup = lastCleanupTime.get(userId);

        // cisti samo ako je proslo više od 10 sekundi od poslednjeg cleanup-a
        if (lastCleanup == null || lastCleanup.isBefore(now.minusSeconds(10))) {
            LocalDateTime oneMinuteAgo = now.minusMinutes(1);
            List<LocalDateTime> requestTimes = userRequestTimes.get(userId);

            if (requestTimes != null) {
                int sizeBefore = requestTimes.size();
                requestTimes.removeIf(time -> time.isBefore(oneMinuteAgo));
                int sizeAfter = requestTimes.size();

                if (requestTimes.isEmpty()) {
                    userRequestTimes.remove(userId);
                    lastCleanupTime.remove(userId);
                } else {
                    lastCleanupTime.put(userId, now);
                }

                if (sizeBefore != sizeAfter) {
                    System.out.println("CLEANUP: User " + userId + " cleaned " + (sizeBefore - sizeAfter) + " old requests");
                }
            }
        }
    }

    public boolean canMakeRequest(Long userId) {
        cleanupIfNeeded(userId); // POZOVI cleanup umesto direktno removeIf()

        List<LocalDateTime> requestTimes = userRequestTimes.computeIfAbsent(userId, k -> new ArrayList<>());
        boolean canMake = requestTimes.size() < MAX_REQUESTS_PER_MINUTE;

        System.out.println("=== GENERAL RATE LIMITER ===");
        System.out.println("User " + userId + " requests: " + requestTimes.size() + "/" + MAX_REQUESTS_PER_MINUTE);
        System.out.println("General limiter allows: " + canMake);

        return canMake;
    }

    public synchronized void recordRequest(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> requestTimes = userRequestTimes.computeIfAbsent(userId, k -> new ArrayList<>());
        requestTimes.add(now);
        System.out.println("GENERAL: Recorded request for user " + userId + " at " + now + ". Total: " + requestTimes.size());
    }

    public int getRemainingRequests(Long userId) {
        // NE poziva cleanup! Koristi postojeće podatke
        List<LocalDateTime> requestTimes = userRequestTimes.computeIfAbsent(userId, k -> new ArrayList<>());
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
