package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.isa.onlybuns_back.model.Following;

import java.util.Date;
import java.util.List;

@Service
public class NotificationService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JavaMailSender mailSender;
    private final FollowingRepository followingRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private static final long TEST_PERIOD_MS = 7L * 24 * 60 * 60 * 1000; // 7 dana u milisekundama

    @Autowired
    public NotificationService(UserRepository userRepository, PostRepository postRepository, JavaMailSender mailSender,
                               FollowingRepository followingRepository, LikeRepository likeRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.mailSender = mailSender;
        this.followingRepository = followingRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
    }

    public void notifyInactiveUsers() {
        long now = System.currentTimeMillis();

        Date inactiveBefore = new Date(now - TEST_PERIOD_MS); // nije se logovao bar 7 dana
        Date notifyBefore   = new Date(now - TEST_PERIOD_MS); // nismo mu slali bar 7 dana

        List<User> targets = userRepository.findInactiveUsersForSummary(inactiveBefore, notifyBefore);

        for (User u : targets) {
            sendSummaryEmail(u);
            u.setLastSummarySentAt(new Date()); // zabelezeno da je poslato
            userRepository.save(u);
        }
    }

    private void sendSummaryEmail(User user) {
        Date sevenDaysAgo = new Date(System.currentTimeMillis() - TEST_PERIOD_MS);
        // 1) novi pratioci ovog korisnika
        long newFollowers = followingRepository.countByFollowedAndFollowedAtAfter(user, sevenDaysAgo);
        // 2) novi lajkovi na postovima ovog korisnika
        long newLikesOnMyPosts = likeRepository.countLikesOnUsersPostsSince(user, sevenDaysAgo);
        // 3) novi komentari na postvoima ovog korisnika
        long newComments = commentRepository.countCommentsOnUsersPostsSince(user, sevenDaysAgo);
        // 4) nove objave ljudi koje ovaj korisnik prati (ako ne prati nikog, broj je 0)
        List<User> following = followingRepository.findAllByFollower(user)
                .stream()
                .map(Following::getFollowed)
                .toList();
        long newPostsFromFollowing = 0;
        if (!following.isEmpty()) {
            newPostsFromFollowing = postRepository.countPostsByAuthorsSince(following, sevenDaysAgo);
        }

        // 5) ukupan broj novih postova na platformi
        int totalNewPosts = postRepository.findByCreationTimeAfter(sevenDaysAgo).size();

        String subject = "Your weekly OnlyBuns Summary 🐇 – we haven’t seen you since " +
                (user.getLastLogin() != null ? new java.text.SimpleDateFormat("MMM dd, yyyy").format(user.getLastLogin()) : "!");
        String body =
                "Hello " + user.getUsername() + "!\n\n" +
                        "Here's what you've missed in the last 7 days:\n" +
                        "- New followers: " + newFollowers + "\n" +
                        "- New likes on your posts: " + newLikesOnMyPosts + "\n" +
                        "- New comments on your posts: " + newComments + "\n" +
                        "- New posts from people you follow: " + newPostsFromFollowing + "\n" +
                        "- Total new posts on the platform: " + totalNewPosts + "\n\n" +
                        "Hop back into OnlyBuns and discover what’s new since you last visited! 🐰";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(user.getEmail());
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }
}
