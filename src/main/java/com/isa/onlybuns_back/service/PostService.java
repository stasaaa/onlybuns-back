package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.PostDto;
import com.isa.onlybuns_back.image.FileStorageService;
import com.isa.onlybuns_back.model.Like;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.LikeRepository;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final LikeRepository likeRepository;

    @Autowired
    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       FileStorageService fileStorageService,
                       LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.likeRepository = likeRepository;
    }

    public PostDto create(PostDto postDto, String imagePath) throws IOException {
        Post post = new Post();
        User user = userRepository.findById(postDto.getUserId()).orElse(null);
        if (user != null) {
            post.setUser(user);
            post.setDescription(postDto.getDescription());
            post.setImagePaths(imagePath);
            post.setLocation(postDto.getAddress());
            post.setCreationTime(new Date());
            post.setComments(new ArrayList<>());

            postRepository.save(post);
            return postDto;
        }
        return null;
    }

    public void delete(Long id) {
        postRepository.deleteById(id);
    }

    public PostDto findById(long id) throws IOException {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return null;

        byte[] image = fileStorageService.getImage(post.getImagePaths());

        PostDto postDto = new PostDto();
        postDto.setDescription(post.getDescription());
        postDto.setAddress(post.getLocation());
        postDto.setUserId(post.getUser().getId());
        postDto.setId(post.getId());
        postDto.setImage(image);
        postDto.setLikes(post.getLikesCount());
        return postDto;
    }

    public Optional<PostDto> update(long id, PostDto postDto) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return Optional.empty();

        post.setDescription(postDto.getDescription());
        postRepository.save(post);
        return Optional.of(postDto);
    }

    public List<Post> getByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }

    public Collection<PostDto> findAll() throws IOException {
        Collection<Post> posts = postRepository.findAll();
        Collection<PostDto> postDtos = new ArrayList<>();
        for (Post post : posts) {
            PostDto postDto = new PostDto();
            postDto.setDescription(post.getDescription());
            postDto.setId(post.getId());
            postDto.setAddress(post.getLocation());
            postDto.setUserId(post.getUser().getId());
            postDto.setCreationTime(post.getCreationTime());
            postDto.setLikes(post.getLikesCount());
            try {
                postDto.setImage(fileStorageService.getImage(post.getImagePaths()));
            } catch (IOException e) {
                throw new IOException(e);
            }

            postDtos.add(postDto);
        }
        return postDtos;
    }

    @Transactional
    public void toggleLike(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());  // unlike
        } else {
            likeRepository.save(new Like(user, post));  // like
        }
    }

    public boolean isLikedByUser(Long postId, Long userId) {
        // koristi metodu koja traži po ID-jevima, da ne moraš praviti User/Post entitete izvan servisa
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }

    public Map<String, Object> getLikeStatus(Long postId, Long userId) {
        // uzmi broj lajkova i da li je user lajkovao
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        boolean liked = likeRepository.existsByUserIdAndPostId(userId, postId);
        long likesCount = likeRepository.countByPost(post);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("likesCount", likesCount);
        return response;
    }
}
