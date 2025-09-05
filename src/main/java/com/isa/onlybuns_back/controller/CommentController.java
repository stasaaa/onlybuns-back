package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.CommentDto;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.CommentRepository;
import com.isa.onlybuns_back.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("comments")
public class CommentController {

    private final CommentService commentService;
    private final CommentRateLimitService rateLimitService;
    private final UserService userService;
    private final PostService postService;
    private final FollowingService followingService;


    @Autowired
    public CommentController(CommentService commentService, CommentRateLimitService rateLimitService, UserService userService, PostService postService, FollowingService followingService) {
        this.commentService = commentService;
        this.rateLimitService = rateLimitService;
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

        // provera rate limita
        if (!rateLimitService.canUserComment(userId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "Rate limit exceeded",
                            "message", "You can only post 60 comments per hour. Please try again later.",
                            "remainingComments", rateLimitService.getRemainingComments(userId)
                    ));
        }

        CommentDto createdComment = commentService.createComment(commentDto);

        // belezenje komentara u rate limit servisu
        rateLimitService.recordComment(userId);

        return ResponseEntity.ok(createdComment);
    }
}
