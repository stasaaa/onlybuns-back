package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.PostDto;
import com.isa.onlybuns_back.image.FileStorageService;
import com.isa.onlybuns_back.model.Address;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import com.isa.onlybuns_back.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("posts")
public class PostController {

    private final PostService postService;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    public PostController(PostService postService, UserService userService, FileStorageService fileStorageService) {
        this.postService = postService;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    @PostMapping("create")
    public ResponseEntity<PostDto> createPost(
            @RequestParam("userId") long userId,
            @RequestParam("description") String description,
            @RequestParam("address") String addressJson,
            @RequestParam("image") MultipartFile imageFile
    ) throws IOException {
        String imagePath = fileStorageService.storeFile(imageFile);
        byte[] imageBytes = imageFile.getBytes();
        PostDto postDto = new PostDto();
        postDto.setDescription(description);
        postDto.setImage(imageBytes);
        postDto.setUserId(userId);

        ObjectMapper objectMapper = new ObjectMapper();
        Address address = objectMapper.readValue(addressJson, Address.class);
        postDto.setAddress(address);

        postService.create(postDto, imagePath);
        return new ResponseEntity<>(postDto, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable long id) throws IOException {
        PostDto ret = postService.findById(id);
        if(ret == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(ret);
    }

    @GetMapping("all")
    public ResponseEntity<Collection<PostDto>> getAllPosts() throws IOException {
        Collection<PostDto> ret = postService.findAll();
        return ResponseEntity.ok(ret);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<PostDto> update(@PathVariable Long id, @RequestBody PostDto postDto) {
        postService.update(id, postDto);
        return ResponseEntity.ok(postDto);
    }

    @GetMapping("user/{userId}")
    public ResponseEntity<List<Post>> getByUserId(@PathVariable long userId) {
        return ResponseEntity.ok(postService.getByUserId(userId));
    }

    @PostMapping("/{postId}/toggle-like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> payload) {

        Long userId = Long.valueOf(payload.get("userId").toString());

        postService.toggleLike(postId, userId);


        Map<String, Object> response = postService.getLikeStatus(postId, userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/liked-by/{userId}")
    public ResponseEntity<Boolean> isLikedByUser(
            @PathVariable Long postId,
            @PathVariable Long userId) {

        boolean liked = postService.isLikedByUser(postId, userId);
        return ResponseEntity.ok(liked);
    }


}
