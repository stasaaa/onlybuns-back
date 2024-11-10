package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.PostDto;
import com.isa.onlybuns_back.image.FileStorageService;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final FileStorageService fileStorageService;
    private final PostService postService;

    @Autowired
    public PostController(FileStorageService fileStorageService, PostService postService) {
        this.fileStorageService = fileStorageService;
        this.postService = postService;
    }

    // Endpoint for uploading a post with images
    @PostMapping("/create")
    public ResponseEntity<PostDto> createPost(@RequestParam("description") PostDto postDto,
                                           @RequestParam("files") List<MultipartFile> files) {
        try {
            List<String> imageUrls = files.stream()
                    .map(fileStorageService::storeFile)
                    .collect(Collectors.toList());

            // Create and save post
            postService.save(postDto, imageUrls);

            return ResponseEntity.ok(postDto); // Return the created post with image URLs
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Endpoint to retrieve a post along with its images
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable long id) {
        Post post = postService.findById(id);

        if (post != null) {
            return ResponseEntity.ok(post); // Return the post with image URLs
        } else {
            return ResponseEntity.status(404).body(null);
        }
    }
}