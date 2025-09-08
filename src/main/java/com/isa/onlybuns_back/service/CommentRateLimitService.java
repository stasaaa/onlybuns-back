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

    public boolean canUserComment(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);

        // dobijanje liste komentara za korisnika
        List<LocalDateTime> commentTimes = userCommentTimes.computeIfAbsent(userId, k -> new ArrayList<>());

        // uklanjanje komentara starije od sat vremena
        commentTimes.removeIf(time -> time.isBefore(oneHourAgo));

        // provera da li korisnik moze da komentarise
        return commentTimes.size() < MAX_COMMENTS_PER_HOUR;
    }

    public void recordComment(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> commentTimes = userCommentTimes.computeIfAbsent(userId, k -> new ArrayList<>());
        commentTimes.add(now);
    }

    public int getRemainingComments(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);

        List<LocalDateTime> commentTimes = userCommentTimes.computeIfAbsent(userId, k -> new ArrayList<>());
        commentTimes.removeIf(time -> time.isBefore(oneHourAgo));

        return MAX_COMMENTS_PER_HOUR - commentTimes.size();
    }
}
