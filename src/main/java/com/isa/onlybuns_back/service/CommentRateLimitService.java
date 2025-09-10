package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.model.Comment;
import com.isa.onlybuns_back.repository.CommentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CommentRateLimitService {
    private static final int MAX_COMMENTS_PER_HOUR = 60;

    @Autowired
    private CommentRepository commentRepository;

    // userId -> lista timestampova komentara
    private final ConcurrentHashMap<Long, List<LocalDateTime>> userCommentTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, LocalDateTime> lastCleanupTime = new ConcurrentHashMap<>();

    // ucitavanje komentara iz baze (okine u trenutku kada se servis kreira)
    @PostConstruct
    public void loadRecentComments() {
        try {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            Date oneHourAgoDate = Date.from(oneHourAgo.atZone(ZoneId.systemDefault()).toInstant());

            // ucitavanje svih komentara iz poslednjeg sata
            List<Comment> recentComments = commentRepository.findByCreationTimeAfter(oneHourAgoDate);

            // grupisanje po userId
            for (Comment comment : recentComments) {
                Long userId = comment.getUser().getId();
                LocalDateTime commentTime = comment.getCreationTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();

                userCommentTimes.computeIfAbsent(userId, k -> new ArrayList<>()).add(commentTime);
            }

            System.out.println("Loaded " + recentComments.size() + " recent comments for rate limiting.");

        } catch (Exception e) {
            System.err.println("Error loading recent comments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void cleanupIfNeeded(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastCleanup = lastCleanupTime.get(userId);
        // cisti samo ako je prošlo više od 30 sekundi od poslednjeg cleanup-a
        if (lastCleanup == null || lastCleanup.isBefore(now.minusSeconds(30))) {
            LocalDateTime oneHourAgo = now.minusHours(1);
            List<LocalDateTime> commentTimes = userCommentTimes.get(userId);

            if (commentTimes != null) {
                int sizeBefore = commentTimes.size();
                commentTimes.removeIf(time -> time.isBefore(oneHourAgo));
                int sizeAfter = commentTimes.size();

                if (commentTimes.isEmpty()) {
                    userCommentTimes.remove(userId);
                    lastCleanupTime.remove(userId);
                } else {
                    lastCleanupTime.put(userId, now);
                }

                if (sizeBefore != sizeAfter) {
                    System.out.println("CLEANUP: User " + userId + " cleaned " + (sizeBefore - sizeAfter) + " old comments");
                }
            }
        }
    }

    public boolean canUserComment(Long userId) {
        cleanupIfNeeded(userId); // samo pozovi cleanup
        List<LocalDateTime> commentTimes = userCommentTimes.computeIfAbsent(userId, k -> new ArrayList<>());
        boolean canComment = commentTimes.size() < MAX_COMMENTS_PER_HOUR;

        System.out.println("=== COMMENT RATE LIMITER ===");
        System.out.println("User " + userId + " comments: " + commentTimes.size() + "/" + MAX_COMMENTS_PER_HOUR);
        System.out.println("Comment limiter allows: " + canComment);

        return canComment;
    }

    public synchronized void recordComment(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> commentTimes = userCommentTimes.computeIfAbsent(userId, k -> new ArrayList<>());
        commentTimes.add(now);
        System.out.println("COMMENT: Recorded comment for user " + userId + " at " + now + " Total: " + commentTimes.size());
    }

    public int getRemainingComments(Long userId) {
        List<LocalDateTime> commentTimes = userCommentTimes.computeIfAbsent(userId, k -> new ArrayList<>());

        return MAX_COMMENTS_PER_HOUR - commentTimes.size();
    }
}
