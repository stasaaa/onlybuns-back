package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.PostDto;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public void save(PostDto postDto, List<String> imageUrls) {
        Post post = new Post();
        User user = userRepository.findById(postDto.getUserId()).orElse(null);
        if(user != null) {
            post.setUser(user);
            post.setDescription(postDto.getDescription());
            post.setImagePaths(imageUrls);
            postRepository.save(post);
        }
    }

    public Post findById(long id) {
        return postRepository.findById(id).orElse(null);
    }
}
