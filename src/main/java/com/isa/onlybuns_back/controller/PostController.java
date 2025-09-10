package com.isa.onlybuns_back.controller;
import com.isa.onlybuns_back.dto.PostDto;
import com.isa.onlybuns_back.image.FileStorageService;
import com.isa.onlybuns_back.model.Address;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import com.isa.onlybuns_back.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;


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
    @PreAuthorize("isAuthenticated()")
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

        postDto.setUserId(userId);

        ObjectMapper objectMapper = new ObjectMapper();
        Address address = objectMapper.readValue(addressJson, Address.class);
        postDto.setAddress(address);

        postService.create(postDto, imagePath);
        return new ResponseEntity<>(postDto, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDto> getPost(@PathVariable long id) throws IOException {
        PostDto ret = postService.findById(id);
        if(ret == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(ret);
    }

    @GetMapping("all")
    public ResponseEntity<Collection<PostDto>> getAllPosts(HttpServletRequest request) throws IOException {
        Collection<PostDto> ret = postService.findAll();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Backend-Port", String.valueOf(request.getLocalPort())); // dodajemo port u header

        return ResponseEntity.ok()
                .headers(headers)
                .body(ret);
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
    public ResponseEntity<List<PostDto>> getByUserId(
            @PathVariable long userId,
            @RequestParam(required = false) Long loggedInUser
    ) {
        return ResponseEntity.ok(postService.getByUserId(userId, loggedInUser));
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

    @GetMapping("post-quantity")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getPostQuantity() {
        Map<String, Long> postQuantity = postService.getPostQuantity();
        return ResponseEntity.ok(postQuantity);
    }

    @GetMapping("five-most-liked-last-week")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Collection<PostDto>> getFiveMostLikedLastWeek() throws IOException {
        return ResponseEntity.ok(postService.getFiveMostLikedLastWeek());
    }

    @GetMapping("top-ten")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Collection<PostDto>> getTopTenPosts() throws IOException {
        return ResponseEntity.ok(postService.getTopTenMostLikedPosts());
    }

    @GetMapping("pagedForUser/{username}/{page}/{pageSize}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Collection<PostDto>> getPaged(
            @PathVariable int page, @PathVariable int pageSize, @PathVariable String username)
            throws IOException {
        return ResponseEntity.ok(postService.getPaged(page,pageSize, username));
    }

    @GetMapping("near-me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Collection<PostDto>> getNearMe(
            @RequestParam String address, @RequestParam int page, @RequestParam int pageSize) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Address userAddress = objectMapper.readValue(address, Address.class);

        return ResponseEntity.ok(postService.getPostsNear(userAddress, page, pageSize));
    }

    @GetMapping("/{postId}/image")
    public ResponseEntity<byte[]> getPostImage(
            @PathVariable Long postId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
            @RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince
    ) throws IOException {

        Post post = postService.findPostEntityById(postId);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }

        // Učitaj sliku kao bajtove
        byte[] imageBytes = fileStorageService.getImage(post.getImagePaths());

        // Generiši ETag na osnovu sadržaja slike (hash)
        String eTag = "\"" + Integer.toHexString(java.util.Arrays.hashCode(imageBytes)) + "\"";

        // Uzmi vreme poslednje izmene fajla (ili iz baze ako imaš)
        // Ovde pretpostavimo da imaš metod u postService koji vraća vreme poslednje izmene slike
        long lastModifiedMillis = postService.getImageLastModified(post);
        // Formatiraj vreme u HTTP format
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.withZone(java.time.ZoneId.of("GMT"));
        String lastModified = formatter.format(java.time.Instant.ofEpochMilli(lastModifiedMillis));

        // Provera ETag
        if (ifNoneMatch != null && ifNoneMatch.equals(eTag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .cacheControl(CacheControl.maxAge(86400, java.util.concurrent.TimeUnit.SECONDS).cachePublic())
                    .eTag(eTag)
                    .build();
        }

        // Provera Last-Modified
        if (ifModifiedSince != null) {
            try {
                long ifModifiedSinceMillis = java.time.ZonedDateTime.parse(ifModifiedSince, formatter).toInstant().toEpochMilli();
                if (ifModifiedSinceMillis >= lastModifiedMillis) {
                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                            .cacheControl(CacheControl.maxAge(86400, java.util.concurrent.TimeUnit.SECONDS).cachePublic())
                            .eTag(eTag)
                            .lastModified(lastModifiedMillis)
                            .build();
                }
            } catch (Exception e) {

            }
        }


        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(86400, java.util.concurrent.TimeUnit.SECONDS).cachePublic())
                .eTag(eTag)
                .lastModified(lastModifiedMillis)
                .contentType(org.springframework.http.MediaType.IMAGE_JPEG) // ili PNG po potrebi
                .body(imageBytes);
    }


}
