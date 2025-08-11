package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.model.Comment;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.CommentRepository;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public Map<String, Long> countPostsByRange(String range) {
        Date startDate = getStartDate(range);
        List<Post> posts = postRepository.findByCreationTimeAfter(startDate);

        SimpleDateFormat formatter = getFormatter(range);

        return posts.stream()
                .collect(Collectors.groupingBy(
                        p -> formatter.format(p.getCreationTime()),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> countCommentsByRange(String range) {
        Date startDate = getStartDate(range);
        List<Comment> comments = commentRepository.findByCreationTimeAfter(startDate);

        SimpleDateFormat formatter = getFormatter(range);

        return comments.stream()
                .collect(Collectors.groupingBy(
                        c -> formatter.format(c.getCreationTime()),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    public Map<String, Double> calculateUserActivityStats() {
        List<User> users = userRepository.findAll();

        long total = users.size();
        long posters = users.stream().filter(u -> !u.getPosts().isEmpty()).count();
        long commenters = users.stream()
                .filter(u -> u.getPosts().isEmpty() && !u.getComments().isEmpty())
                .count();
        long inactive = total - posters - commenters;

        Map<String, Double> result = new HashMap<>();
        result.put("posted", total == 0 ? 0 : posters * 100.0 / total);
        result.put("commentedOnly", total == 0 ? 0 : commenters * 100.0 / total);
        result.put("inactive", total == 0 ? 0 : inactive * 100.0 / total);
        return result;
    }

    private Date getStartDate(String range) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = switch (range.toLowerCase()) {
            case "weekly" -> now.minusWeeks(12);
            case "monthly" -> now.minusMonths(12);
            case "yearly" -> now.minusYears(5);
            default -> throw new IllegalArgumentException("Invalid range: " + range);
        };
        return Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private SimpleDateFormat getFormatter(String range) {
        return switch (range.toLowerCase()) {
            case "weekly" -> new SimpleDateFormat("YYYY-'W'ww");
            case "monthly" -> new SimpleDateFormat("yyyy-MM");
            case "yearly" -> new SimpleDateFormat("yyyy");
            default -> throw new IllegalArgumentException("Invalid range: " + range);
        };
    }
}
