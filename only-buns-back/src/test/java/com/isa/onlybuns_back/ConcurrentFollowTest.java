package com.isa.onlybuns_back;

import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.model.UserRole;
import com.isa.onlybuns_back.repository.GroupChatRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import com.isa.onlybuns_back.repository.FollowingRepository;
import com.isa.onlybuns_back.service.FollowingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrentFollowTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupChatRepository groupChatRepository;

    @Autowired
    private FollowingRepository followingRepository;

    @Autowired
    private FollowingService followingService;

    private User targetUser;

    @BeforeEach
    void setUp() {
        // očisti sve podatke da izbegneš FK constraint probleme
        groupChatRepository.deleteAll();
        followingRepository.deleteAll();
        userRepository.deleteAll();

        // kreiraj target korisnika
        targetUser = new User("test@test.com", "target", "pass", "Target", "User",
                UserRole.REGISTERED, new Date(), true, null, null);
        userRepository.save(targetUser);

        // kreiraj 20 followera
        for (int i = 0; i < 20; i++) {
            User follower = new User("follower" + i + "@test.com", "follower" + i, "pass",
                    "F", "L", UserRole.REGISTERED, new Date(), true, null, null);
            userRepository.save(follower);
        }
    }

    @Test
    void testConcurrentFollow() throws InterruptedException {
        List<User> followers = userRepository.findAll().stream()
                .filter(u -> u.getUsername().startsWith("follower"))
                .toList();

        CountDownLatch latch = new CountDownLatch(followers.size());
        List<Thread> threads = new ArrayList<>();

        for (User follower : followers) {
            Thread t = new Thread(() -> {
                try {
                    followingService.follow(follower.getUsername(), targetUser.getUsername());
                } finally {
                    latch.countDown();
                }
            });
            threads.add(t);
            t.start();
        }

        latch.await();  // sačekaj sve threadove

        long followersCount = followingService.countFollowers(targetUser.getUsername());
        assertThat(followersCount).isEqualTo(20);
    }
}
