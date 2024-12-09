package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.PostDto;
import com.isa.onlybuns_back.image.FileStorageService;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.io.IOException;

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

    public PostDto create(PostDto postDto, String imagePath) throws IOException {
        Post post = new Post();
        User user = userRepository.findById(postDto.getUserId()).orElse(null);
        if (user != null) {
            post.setUser(user);
            post.setDescription(postDto.getDescription());
            post.setImagePaths(imagePath);
            post.setLocation(postDto.getAddress());
            post.setCreationTime(new Date());
            post.setLikes(0);
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

    public Optional<PostDto> update(long id, PostDto postDto) {
        Post post = postRepository.findById(id).orElse(null);
        post.setDescription(postDto.getDescription());
        postRepository.save(post);
        return postDto.equals(post) ? Optional.of(postDto) : Optional.empty();
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
            postDto.setLikes(post.getLikes());
            try{
                postDto.setImage(fileStorageService.getImage(post.getImagePaths()));
            } catch (IOException e) {
                throw new IOException(e);
            }

            postDtos.add(postDto);
        }
        return postDtos;
    }
    public void toggleLike(Long postId, boolean liked) {
        Post post = postRepository.findById(postId).orElse(null);

        // Increase or decrease likes based on the `liked` parameter
        if (liked) {
            post.setLikes(post.getLikes() + 1); // Increment likes
        } else {
            post.setLikes(post.getLikes() - 1); // Decrement likes
        }

        postRepository.save(post);
    }

    public Map<String, Long> getPostQuantity() {
        long all = postRepository.count();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date lastMonthDate = calendar.getTime();
        long lastMonth = postRepository.countPostsFromLastMonth(lastMonthDate);

        Map<String, Long> result = new HashMap<>();
        result.put("totalPosts", all);
        result.put("lastMonthPosts", lastMonth);
        return result;
    }

    public Collection<PostDto> getFiveMostLikedLastWeek() throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);  // Datum od pre 7 dana
        Date sevenDaysAgo = calendar.getTime();

        Pageable topFive = (Pageable) PageRequest.of(0, 5);

        List<Post> posts = postRepository.getFiveMostLikedLastWeek(sevenDaysAgo, topFive);
        Collection<PostDto> postDtos = new ArrayList<>();
        for (Post post : posts) {
            PostDto postDto = new PostDto();
            postDto.setDescription(post.getDescription());
            postDto.setId(post.getId());
            postDto.setAddress(post.getLocation());
            postDto.setUserId(post.getUser().getId());
            postDto.setCreationTime(post.getCreationTime());
            postDto.setLikes(post.getLikes());
            try{
                postDto.setImage(fileStorageService.getImage(post.getImagePaths()));
            } catch (IOException e) {
                throw new IOException(e);
            }

            postDtos.add(postDto);
        }
        return postDtos;
    }

    public Collection<PostDto> getTopTenMostLikedPosts() throws IOException {
        Pageable topTen = (Pageable) PageRequest.of(0, 10);  // Podesi broj na 10
        List<Post> posts = postRepository.getTopTenMostLikedPosts(topTen);
        Collection<PostDto> postDtos = new ArrayList<>();
        for (Post post : posts) {
            PostDto postDto = new PostDto();
            postDto.setDescription(post.getDescription());
            postDto.setId(post.getId());
            postDto.setAddress(post.getLocation());
            postDto.setUserId(post.getUser().getId());
            postDto.setCreationTime(post.getCreationTime());
            postDto.setLikes(post.getLikes());
            try{
                postDto.setImage(fileStorageService.getImage(post.getImagePaths()));
            } catch (IOException e) {
                throw new IOException(e);
            }

            postDtos.add(postDto);
        }
        return postDtos;
    }
}
