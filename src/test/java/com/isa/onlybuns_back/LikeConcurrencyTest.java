package com.isa.onlybuns_back;

import com.isa.onlybuns_back.model.Address;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.model.UserRole;
import com.isa.onlybuns_back.repository.LikeRepository;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import com.isa.onlybuns_back.service.PostService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest(properties = "spring.profiles.active=test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LikeConcurrencyTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    private Long postId;
    private List<Long> userIds = new ArrayList<>();
    private Long creatorId;

    @BeforeAll
    public void setUp() {
        // kreira kreatora posta
        User creator = new User();
        creator.setUsername("creator_" + UUID.randomUUID());
        creator.setPassword("test");
        creator.setEmail("creator_" + UUID.randomUUID() + "@example.com");
        creator.setFirstName("Creator");
        creator.setLastName("Test");
        creator.setUserRole(UserRole.REGISTERED);
        creator.setActive(true);
        userRepository.save(creator);
        creatorId = creator.getId();

        // kreira post
        Post post = new Post();
        post.setDescription("Test Post");
        post.setImagePaths("test.jpg");
        post.setLocation(new Address());
        post.setCompressed(false);
        post.setUser(creator);
        post.setCreationTime(new Date());
        postRepository.save(post);
        postId = post.getId();

        // kreira 10 korisnika koji ce lajkovati post
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setUsername("user" + i + "_" + UUID.randomUUID());
            user.setPassword("pass");
            user.setEmail("user" + i + "_" + UUID.randomUUID() + "@example.com");
            user.setFirstName("User" + i);
            user.setLastName("Test");
            user.setUserRole(UserRole.REGISTERED);
            user.setActive(true);
            userRepository.save(user);
            userIds.add(user.getId());
        }
    }

    @Test
    public void testConcurrentLikes() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (Long userId : userIds) {
            executor.submit(() -> postService.toggleLike(postId, userId));
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        Post post = postRepository.findById(postId).orElseThrow();
        long likeCount = likeRepository.countByPost(post);

        System.out.println("Total number of likes: " + likeCount);
        Assertions.assertEquals(userIds.size(), likeCount);
    }

    @AfterAll
    public void cleanUp() {
        // obrise lajkove za test post
        likeRepository.deleteAll(
                likeRepository.findAll().stream()
                        .filter(like -> like.getPost().getId() == postId)
                        .toList()
        );

        // obrise post
        postRepository.findById(postId).ifPresent(postRepository::delete);

        // obrise korisnike koji su lajkovali
        userIds.forEach(id ->
                userRepository.findById(id).ifPresent(userRepository::delete)
        );

        // obrise kreatora posta
        userRepository.findById(creatorId).ifPresent(userRepository::delete);
    }

}