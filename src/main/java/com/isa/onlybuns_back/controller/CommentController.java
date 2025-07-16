package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.CommentDto;
import com.isa.onlybuns_back.repository.CommentRepository;
import com.isa.onlybuns_back.service.CommentRateLimitService;
import com.isa.onlybuns_back.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("comments")
public class CommentController {

    private final CommentService commentService;
    private final CommentRateLimitService rateLimitService;

    @Autowired
    public CommentController(CommentService commentService, CommentRateLimitService rateLimitService) {
        this.commentService = commentService;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDto>> getCommentsForPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsForPost(postId));
    }

    @PostMapping("/new")
    public ResponseEntity<?> createComment(@RequestBody CommentDto commentDto) {
        Long userId = commentDto.getUserId();

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
