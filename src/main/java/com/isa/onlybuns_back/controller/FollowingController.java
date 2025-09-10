package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.service.FollowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/following")
@RequiredArgsConstructor
public class FollowingController {

    private final FollowingService followingService;

    @PostMapping("/follow/{username}")
    public ResponseEntity<?> follow(@PathVariable String username, Principal principal) {
        followingService.follow(principal.getName(), username);
        return ResponseEntity.ok(Map.of("status", "followed"));
    }

    @DeleteMapping("/unfollow/{username}")
    public ResponseEntity<?> unfollow(@PathVariable String username, Principal principal) {
        followingService.unfollow(principal.getName(), username);
        return ResponseEntity.ok(Map.of("status", "unfollowed"));
    }

    // Lista korisnika koje ja pratim
    @GetMapping("/{username}/followed")
    public List<UserDto> getFollowed(@PathVariable String username) {
        return followingService.getFollowedUsers(username);
    }

    // Lista mojih pratilaca
    @GetMapping("/{username}/followers")
    public List<UserDto> getFollowers(@PathVariable String username) {
        return followingService.getFollowers(username);
    }

    // Broj pratilaca (kao JSON)
    @GetMapping("/{username}/followers/count")
    public ResponseEntity<Map<String, Long>> countFollowers(@PathVariable String username) {
        long count = followingService.countFollowers(username);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // Broj koje korisnik prati (kao JSON)
    @GetMapping("/{username}/following/count")
    public ResponseEntity<Map<String, Long>> countFollowing(@PathVariable String username) {
        long count = followingService.countFollowing(username);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // Provera da li je trenutno ulogovani korisnik već zapratio
    @GetMapping("/is-following/{username}")
    public ResponseEntity<Map<String, Boolean>> isFollowing(@PathVariable String username, Principal principal) {
        boolean following = followingService.isFollowing(principal.getName(), username);
        return ResponseEntity.ok(Map.of("isFollowing", following));
    }
}
