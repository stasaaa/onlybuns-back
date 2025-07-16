package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.service.FollowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/following")
@RequiredArgsConstructor
public class FollowingController {

    private final FollowingService followingService;

    @PostMapping("/follow/{username}")
    public ResponseEntity<?> follow(@PathVariable String username, Principal principal) {
        followingService.follow(principal.getName(), username);
        return ResponseEntity.ok("Followed");
    }

    @DeleteMapping("/unfollow/{username}")
    public ResponseEntity<?> unfollow(@PathVariable String username, Principal principal) {
        followingService.unfollow(principal.getName(), username);
        return ResponseEntity.ok("Unfollowed");
    }

    @GetMapping("/followed")
    public List<User> getFollowed(Principal principal) {
        return followingService.getFollowedUsers(principal.getName());
    }

    @GetMapping("/followers")
    public List<User> getFollowers(Principal principal) {
        return followingService.getFollowers(principal.getName());
    }
}

