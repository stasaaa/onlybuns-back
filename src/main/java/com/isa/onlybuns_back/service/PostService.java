package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.PostDto;
import com.isa.onlybuns_back.image.FileStorageService;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository, FileStorageService fileStorageService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public PostDto save(PostDto postDto, String imagePath) throws IOException {
        Post post = new Post();
        User user = userRepository.findById(postDto.getUserId()).orElse(null);
        if (user != null) {
            post.setUser(user);
            post.setDescription(postDto.getDescription());
            post.setImagePaths(imagePath);
            post.setLocation(postDto.getAddress());
            postRepository.save(post);
            return postDto;
        }
        return null;
    }

    public PostDto findById(long id) throws IOException {
        // Fetch the post from repository
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return null;  // Or throw an exception if you want to handle this case
        }

        // Get the image bytes
        byte[] image = fileStorageService.getImage(post.getImagePaths());

        // Create a new PostDto
        PostDto postDto = new PostDto();
        postDto.setDescription(post.getDescription());
        postDto.setAddress(post.getLocation());
        postDto.setUserId(post.getUser().getId());
        postDto.setId(post.getId());
        postDto.setImage(image);

        return postDto;  // Correct return
    }

    public Collection<PostDto> findAll() {
        Collection<Post> posts = postRepository.findAll();
        Collection<PostDto> postDtos = new ArrayList<>();
        posts.forEach(post -> {
            PostDto postDto = new PostDto();
            postDto.setDescription(post.getDescription());
            postDto.setAddress(post.getLocation());
            postDto.setId(post.getId());
            postDto.setUserId(post.getUser().getId());
            try {
                postDto.setImage(fileStorageService.getImage(post.getImagePaths()));
                postDtos.add(postDto);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return postDtos;
    }
}