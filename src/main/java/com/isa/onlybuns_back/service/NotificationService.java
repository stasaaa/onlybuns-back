package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NotificationService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JavaMailSender mailSender;

    @Autowired
    public NotificationService(UserRepository userRepository, PostRepository postRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.mailSender = mailSender;
    }

    public void notifyInactiveUsers() {
        Date sevenDaysAgo = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L);
        List<User> inactiveUsers = userRepository.findByLastLoginBefore(sevenDaysAgo);

        for (User user : inactiveUsers) {
            sendSummaryEmail(user);
        }
    }

    private void sendSummaryEmail(User user) {
        Date sevenDaysAgo = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L);
        //if the user has been inactive for seven days, there is no need for removing their posts from the list cause their existence would be impossible
        List<Post> recentPosts = postRepository.findByCreationTimeAfter(sevenDaysAgo);
        int newPosts = recentPosts.size();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your Weekly Summary");
        message.setText("Hello " + user.getUsername() + ",\n\n" +
                "Here is your summary for the past week:\n" +
                "- New followers: 5\n" +
                "- New likes: 20\n" +
                "- New posts: " + newPosts + "\n\n" +
                "Visit our app to see more!");
        mailSender.send(message);
    }
}
