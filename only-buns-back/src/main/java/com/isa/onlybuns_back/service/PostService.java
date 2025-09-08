package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.PostDto;
import com.isa.onlybuns_back.image.FileStorageService;
import com.isa.onlybuns_back.mapper.PostMapper;
import com.isa.onlybuns_back.model.Like;
import com.isa.onlybuns_back.model.Address;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.LikeRepository;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final LikeRepository likeRepository;

    @Autowired
    private LocationService locationService;

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
        // validacija za duzinu opisa
        String desc = postDto.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            throw new RuntimeException("Description cannot be empty.");
        }
        if (desc.length() > 250) {
            throw new RuntimeException("Description cannot exceed 250 characters.");
        }

        Post post = new Post();
        User user = userRepository.findById(postDto.getUserId()).orElse(null);
        if (user != null) {
            post.setUser(user);
            post.setDescription(postDto.getDescription());
            post.setImagePaths(imagePath);
            //post.setLocation(postDto.getAddress());
            System.out.println("DEBUG: Pozivam locationService.cachePostLocation()");
            Address cachedLocation = locationService.cachePostLocation(postDto.getAddress());
            post.setLocation(cachedLocation);
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
        postDto.setLikes(post.getLikesCount());
        return postDto;
    }

    public Optional<PostDto> update(long id, PostDto postDto) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return Optional.empty();

        // validacija za duzinu opisa
        String desc = postDto.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            throw new RuntimeException("Description cannot be empty.");
        }
        if (desc.length() > 250) {
            throw new RuntimeException("Description cannot exceed 250 characters.");
        }

        post.setDescription(postDto.getDescription());
        postRepository.save(post);
        return Optional.of(postDto);
    }

    public List<PostDto> getByUserId(Long userId, Long loggedInUserId) {
        if (loggedInUserId == null) {
            return postRepository.findByUserId(userId)
                    .stream()
                    .map(PostMapper::toDto)
                    .sorted((p1, p2) -> p2.getCreationTime().compareTo(p1.getCreationTime()))
                    .toList();
        }
        return postRepository.findByUserId(userId)
                .stream()
                .map(p -> {
                    boolean isLiked = isLikedByUser(p.getId(), loggedInUserId);
                    return PostMapper.toDto(p, isLiked);
                })
                .sorted((p1, p2) -> p2.getCreationTime().compareTo(p1.getCreationTime()))
                .toList();
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


            postDtos.add(postDto);
        }
        return postDtos;
    }

    @Transactional
    public void toggleLike(Long postId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());  // unlike
        } else {
            likeRepository.save(new Like(user, post));  // like
        }
        // simulacija za konkurentno testiranje
        if ("test".equals(System.getProperty("spring.profiles.active"))) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
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

    @Cacheable(value = "topPostsLast7Days", key = "'top-5-week'")
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
            postDto.setLikes(post.getLikesCount());


            postDtos.add(postDto);
        }
        return postDtos;
    }

    @Cacheable(value = "topPostsAllTime", key = "'top-10'")
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
            postDto.setLikes(post.getLikesCount());


            postDtos.add(postDto);
        }
        return postDtos;
    }

    public Collection<PostDto> getPaged(int page, int pageSize, String username) throws IOException {
        // Create pageable instance
        Pageable pageable = PageRequest.of(page, pageSize);

        // Fetch paginated posts for the specific user by username
        Page<Post> posts = postRepository.findByUsernamePaged(username, pageable);

        // Convert posts to PostDto
        Collection<PostDto> postDtos = new ArrayList<>();
        for (Post post : posts) {
            PostDto postDto = new PostDto();
            postDto.setDescription(post.getDescription());
            postDto.setId(post.getId());
            postDto.setAddress(post.getLocation());
            postDto.setUserId(post.getUser().getId());
            postDto.setCreationTime(post.getCreationTime());
            postDto.setLikes(post.getLikesCount());

            postDtos.add(postDto);
        }
        return postDtos;
    }

    public Collection<PostDto> getPostsNear(Address address, int page, int pageSize) throws IOException {
        double userLat = address.getLatitude();
        double userLon = address.getLongitude();

        // Assume posts is a list of all posts (to be retrieved from a database)
        List<Post> allPosts = postRepository.findAll();

        // Filter posts within the 500m radius
        List<Post> filteredPosts = allPosts.stream()
                .filter(post -> {
                    double postLat = post.getLocation().getLatitude();
                    double postLon = post.getLocation().getLongitude();
                    return haversine(userLat, userLon, postLat, postLon) <= 500;
                })
                .skip((long) page * pageSize)
                .limit(pageSize)
                .toList();

        Collection<PostDto> postDtos = new ArrayList<>();
        for (Post post : filteredPosts) {
            PostDto postDto = new PostDto();
            postDto.setDescription(post.getDescription());
            postDto.setId(post.getId());
            postDto.setAddress(post.getLocation());
            postDto.setUserId(post.getUser().getId());
            postDto.setCreationTime(post.getCreationTime());
            postDto.setLikes(post.getLikesCount());

            postDtos.add(postDto);
        }
        return postDtos;
    }

    // Method to convert degrees to radians
    private static double toRadians(double degree) {
        return degree * (Math.PI / 180);
    }

    // Haversine formula to calculate distance between two lat/lng points
    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Radius of the earth in meters
        double latDistance = toRadians(lat2 - lat1);
        double lonDistance = toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in meters
    }

    public Post findPostEntityById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    public long getImageLastModified(Post post) {
        Path imagePath = Paths.get(post.getImagePaths());
        try {
            return Files.getLastModifiedTime(imagePath).toMillis();
        } catch (IOException e) {
            return System.currentTimeMillis(); // fallback ako nema info
        }
    }


}
