package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.PostDto;
import com.isa.onlybuns_back.image.FileStorageService;
import com.isa.onlybuns_back.model.Address;
import com.isa.onlybuns_back.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;

@RestController
@RequestMapping("posts")
public class PostController {

    private final FileStorageService fileStorageService;
    private final PostService postService;

    @Autowired
    public PostController(FileStorageService fileStorageService, PostService postService) {
        this.fileStorageService = fileStorageService;
        this.postService = postService;
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

        postService.save(postDto, imagePath);
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
    public ResponseEntity<Collection<PostDto>> getAllPosts() {
        Collection<PostDto> ret = postService.findAll();
        return ResponseEntity.ok(ret);
    }
}