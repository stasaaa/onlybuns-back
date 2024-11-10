package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.iservice.IPostService;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PostService implements IPostService {

    @Autowired
    private PostRepository postRepository;

    @Override
    public Optional<Post> getById(Long id) {
        return postRepository.findById(id);
    }

    @Override
    public List<Post> getAll() {
        return postRepository.findAll();
    }

    @Override
    public Post create(Post post) {
        post.setCreationTime(new Date());
        return postRepository.save(post);
    }

    @Override
    public void delete(Long id) {
        postRepository.deleteById(id);
    }

    @Override
    public Optional<Post> update(Long id, Post postDetails) {
        return postRepository.findById(id).map(post -> {
            post.setDescription(postDetails.getDescription());
            post.setImage(postDetails.getImage());
            post.setLocation(postDetails.getLocation());
            post.setLikes(postDetails.getLikes());
            post.setUser(postDetails.getUser());
            post.setComments(postDetails.getComments());
            return postRepository.save(post);
        });
    }

    @Override
    public List<Post> getByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }
}
