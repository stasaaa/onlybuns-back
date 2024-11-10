package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.dto.PostCreateDto;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.service.PostService;
import com.isa.onlybuns_back.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @GetMapping("{id}")
    public ResponseEntity<Post> getById(@PathVariable Long id) {
        return postService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAll() {
        return ResponseEntity.ok(postService.getAll());
    }

    @PostMapping("create")
    public ResponseEntity<?> createPost(@RequestBody PostCreateDto postDTO) {
        try {
            // nalazi usera po id-u
            User user = userService.findById(postDTO.getUserId());
            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }

            Post post = new Post();
            post.setDescription(postDTO.getDescription());
            post.setImage(postDTO.getImage());
            post.setLocation(postDTO.getLocation());
            post.setCreationTime(new Date());
            post.setLikes(0);
            post.setUser(user);

            Post savedPost = postService.create(post);
            return ResponseEntity.status(201).body(savedPost);

        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> update(@PathVariable Long id, @RequestBody Post post) {
        return postService.update(id, post)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getByUserId(@PathVariable long userId) {
        return ResponseEntity.ok(postService.getByUserId(userId));
    }
}
