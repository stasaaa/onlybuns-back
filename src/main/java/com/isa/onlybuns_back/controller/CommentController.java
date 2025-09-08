package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.CommentDto;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.CommentRepository;
import com.isa.onlybuns_back.service.*;
import com.isa.onlybuns_back.service.CommentRateLimitService;
import com.isa.onlybuns_back.service.CommentService;
import jakarta.validation.Valid;
import com.isa.onlybuns_back.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("comments")
public class CommentController {

    private final CommentService commentService;
    private final CommentRateLimitService commentRateLimitService;
    private final RateLimiterService rateLimiterService;
    private final UserService userService;
    private final PostService postService;
    private final FollowingService followingService;


    @Autowired
    public CommentController(CommentService commentService, CommentRateLimitService commentRateLimitService, RateLimiterService rateLimiterService, UserService userService, PostService postService, FollowingService followingService) {
        this.commentService = commentService;
        this.commentRateLimitService = commentRateLimitService;
        this.rateLimiterService = rateLimiterService;
        this.userService = userService;
        this.postService = postService;
        this.followingService = followingService;
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDto>> getCommentsForPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsForPost(postId));
    }

    @PostMapping("/new")
    public ResponseEntity<?> createComment(@Valid @RequestBody CommentDto commentDto, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be logged in to comment.");
        }

        // provera da li korisnik postoji
        Long userId = commentDto.getUserId();
        User user = userService.getEntityByUsername(principal.getName());
        if (user == null || user.getId() != (userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid user.");
        }

        // provera da li prati autora posta
        Long postId = commentDto.getPostId();
        Post post = postService.findPostEntityById(postId);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found.");
        }

        // dozvoli ako je autor post-a ili ako prati autora
        User postAuthor = post.getUser();
        if (user.getId() != (postAuthor.getId()) &&
                !followingService.isFollowing(user.getUsername(), postAuthor.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You must follow the author to comment on this post.");
        }

        // genericki rate limit (5 zahteva po minuti)
        if (!rateLimiterService.canMakeRequest(userId)) {
            LocalDateTime nextAvailable = rateLimiterService.getNextAvailableTime(userId);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "General rate limit exceeded",
                            "message", "You can only make 5 requests per minute. Please try again later.",
                            "remainingRequests", rateLimiterService.getRemainingRequests(userId),
                            "nextAvailableTime", nextAvailable != null ? nextAvailable.toString() : "now"
                    ));
        }

        // provera rate limita za komentare (60 po satu)
        if (!commentRateLimitService.canUserComment(userId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "Comment rate limit exceeded",
                            "message", "You can only post 60 comments per hour. Please try again later.",
                            "remainingComments", commentRateLimitService.getRemainingComments(userId)
                    ));
        }

        try {
            CommentDto createdComment = commentService.createComment(commentDto);

            rateLimiterService.recordRequest(userId);
            commentRateLimitService.recordComment(userId);

            return ResponseEntity.ok(createdComment);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to create comment",
                            "message", e.getMessage()
                    ));
        }
    }

    // endpoint za pracenje rate limit statusa
    @GetMapping("/rate-limit-status/{userId}")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(@PathVariable Long userId) {
        try {
            LocalDateTime nextTime = rateLimiterService.getNextAvailableTime(userId);

            Map<String, Object> generalRateLimit = new HashMap<>();
            generalRateLimit.put("canMakeRequest", rateLimiterService.canMakeRequest(userId));
            generalRateLimit.put("remainingRequests", rateLimiterService.getRemainingRequests(userId));
            generalRateLimit.put("nextAvailableTime", nextTime != null ? nextTime.toString() : null);

            Map<String, Object> commentRateLimit = new HashMap<>();
            commentRateLimit.put("canComment", commentRateLimitService.canUserComment(userId));
            commentRateLimit.put("remainingComments", commentRateLimitService.getRemainingComments(userId));

            Map<String, Object> response = new HashMap<>();
            response.put("generalRateLimit", generalRateLimit);
            response.put("commentRateLimit", commentRateLimit);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
