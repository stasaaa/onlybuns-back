package com.isa.onlybuns_back.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LoginAttemptService {

    // Cache za praćenje IP adresa i broja pokušaja prijave
    private final Cache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        this.attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES) // resetuje broj pokušaja svakih minut
                .maximumSize(1000) // limitira broj entry-a u cache
                .build();
    }

    public void loginSucceeded(String ipAddress) {
        attemptsCache.invalidate(ipAddress);
    }

    public void loginFailed(String ipAddress) {
        Integer attempts = attemptsCache.getIfPresent(ipAddress);
        attempts = (attempts == null) ? 1 : attempts + 1;
        attemptsCache.put(ipAddress, attempts);
    }

    public boolean isBlocked(String ipAddress) {
        Integer attempts = attemptsCache.getIfPresent(ipAddress);
        return attempts != null && attempts >= 5; // Ograničenje na 5 pokušaja
    }
}