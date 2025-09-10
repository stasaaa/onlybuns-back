package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.mapper.UserMapper;
import com.isa.onlybuns_back.model.Following;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.model.UserRole;
import com.isa.onlybuns_back.repository.FollowingRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class FollowingService {

    private final FollowingRepository followingRepository;
    private final UserRepository userRepository;


    private final Map<String, List<Long>> followTimestamps = new ConcurrentHashMap<>();

    private boolean canFollow(String username) {
        long now = System.currentTimeMillis();
        followTimestamps.putIfAbsent(username, new ArrayList<>());
        List<Long> timestamps = followTimestamps.get(username);

        // ukloni sve starije od 60 sekundi
        timestamps.removeIf(ts -> now - ts > 60_000);

        if (timestamps.size() >= 50) {
            return false;
        }
        timestamps.add(now);
        return true;
    }

    public long countFollowing(String username) {
        User user = userRepository.findByUsername(username);
        return followingRepository.countByFollower(user);
    }

    @Transactional
    public void follow(String followerUsername, String followedUsername) {
        if (!canFollow(followerUsername)) {
            throw new RuntimeException("Rate limit exceeded. Max 50 follows per minute.");
        }

        User follower = userRepository.findByUsername(followerUsername);
        // Zaključavanje target user-a za siguran upis (sprečava dupli insert)
        User followed = userRepository.findWithLockingByUsername(followedUsername);

        if (follower == null || followed == null || follower.equals(followed)) {
            throw new RuntimeException("Invalid follow request.");
        }
        if (follower.getUserRole() == UserRole.ADMIN) {
            throw new RuntimeException("Admins cannot follow users.");
        }
        if (followingRepository.existsByFollowerAndFollowed(follower, followed)) {
            throw new RuntimeException("Already following.");
        }

        // simulacija za konkurentno testiranje
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        followingRepository.save(new Following(follower, followed));
    }

    @Transactional
    public void unfollow(String followerUsername, String followedUsername) {
        User follower = userRepository.findByUsername(followerUsername);
        User followed = userRepository.findByUsername(followedUsername);
        if (follower == null || followed == null) {
            throw new RuntimeException("Invalid unfollow request.");
        }
        followingRepository.deleteByFollowerAndFollowed(follower, followed);
    }

    public List<UserDto> getFollowedUsers(String username) {
        User user = userRepository.findByUsername(username);
        return followingRepository.findAllByFollower(user)
                .stream()
                .map(Following::getFollowed)
                .map(UserMapper::toDto)
                .toList();
    }

    public List<UserDto> getFollowers(String username) {
        User user = userRepository.findByUsername(username);
        return followingRepository.findAllByFollowed(user)
                .stream()
                .map(Following::getFollower)
                .map(UserMapper::toDto)
                .toList();
    }

    public long countFollowers(String username) {
        User user = userRepository.findByUsername(username);
        return followingRepository.countByFollowed(user);
    }

    public boolean isFollowing(String followerUsername, String followedUsername) {
        User follower = userRepository.findByUsername(followerUsername);
        User followed = userRepository.findByUsername(followedUsername);
        return followingRepository.existsByFollowerAndFollowed(follower, followed);
    }
}
