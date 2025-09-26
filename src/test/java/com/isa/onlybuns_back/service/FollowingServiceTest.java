package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.model.Following;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.model.UserRole;
import com.isa.onlybuns_back.repository.FollowingRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FollowingServiceTest {

    private FollowingRepository followingRepository;
    private UserRepository userRepository;
    private FollowingService followingService;

    private final User follower = new User();
    private final User followed = new User();

    @BeforeEach
    void setup() {
        followingRepository = mock(FollowingRepository.class);
        userRepository = mock(UserRepository.class);

        follower.setId(1L);
        follower.setUsername("follower");
        follower.setUserRole(UserRole.REGISTERED);

        followed.setId(2L);
        followed.setUsername("followed");
        followed.setUserRole(UserRole.REGISTERED);

        when(userRepository.findByUsername("follower")).thenReturn(follower);
        when(userRepository.findWithLockingByUsername("followed")).thenReturn(followed);

        when(followingRepository.existsByFollowerAndFollowed(follower, followed)).thenReturn(false);

        followingService = new FollowingService(followingRepository, userRepository);
    }

    @Test
    void testFollowRateLimit() {
        // Dozvoljeno je 50 follow po minuti
        AtomicInteger successCount = new AtomicInteger();
        for (int i = 0; i < 50; i++) {
            assertDoesNotThrow(() -> {
                followingService.follow("follower", "followed");
                successCount.incrementAndGet();
            });
        }

        assertEquals(50, successCount.get());

        // Sledeći pokušaj treba da baci RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            followingService.follow("follower", "followed");
        });
        assertEquals("Rate limit exceeded. Max 50 follows per minute.", exception.getMessage());
    }

    @Test
    void testFollowAddsRecord() {
        followingService.follow("follower", "followed");

        ArgumentCaptor<Following> captor = ArgumentCaptor.forClass(Following.class);
        verify(followingRepository, times(1)).save(captor.capture());

        Following saved = captor.getValue();
        assertEquals(follower, saved.getFollower());
        assertEquals(followed, saved.getFollowed());
    }
    @Test
    void testFollowRateLimitWithConsoleLog() {
        int totalAttempts = 100;
        int successCount = 0;

        for (int i = 1; i <= totalAttempts; i++) {
            try {
                followingService.follow("follower", "followed");
                successCount++;
                System.out.println("Attempt " + i + ": SUCCESS");
            } catch (RuntimeException e) {
                System.out.println("Attempt " + i + ": FAILED - " + e.getMessage());
            }
        }

        System.out.println("Total successful follows: " + successCount);

        // Proverimo da je uspešno 50
        assertEquals(50, successCount);

        // Proverimo da je save pozvano 50 puta
        ArgumentCaptor<Following> captor = ArgumentCaptor.forClass(Following.class);
        verify(followingRepository, atMost(50)).save(captor.capture());
        assertEquals(50, captor.getAllValues().size());
    }

}
