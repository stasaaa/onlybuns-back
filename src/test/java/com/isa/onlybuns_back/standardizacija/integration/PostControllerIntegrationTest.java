package com.isa.onlybuns_back.standardizacija.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isa.onlybuns_back.controller.PostController;
import com.isa.onlybuns_back.image.FileStorageService;
import com.isa.onlybuns_back.model.Address;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.LikeRepository;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import com.isa.onlybuns_back.service.LocationService;
import com.isa.onlybuns_back.service.PostService;
import com.isa.onlybuns_back.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isIn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.isa.onlybuns_back.security.JWTAuthenticationFilter;
import com.isa.onlybuns_back.security.JWTService;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PostController.class)
@Import(PostService.class)
class PostControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean PostRepository postRepository;
    @MockBean UserRepository userRepository;
    @MockBean LikeRepository likeRepository;
    @MockBean FileStorageService fileStorageService;
    @MockBean LocationService locationService;
    @MockBean UserService userService;
    @MockBean JWTService jwtService;
    @MockBean JWTAuthenticationFilter jwtAuthenticationFilter;

    private User user;
    private Address address;

    @BeforeEach
    void init() {
        user = new User();
        user.setId(7L);
        user.setUsername("neo");

        address = new Address();
        address.setLatitude(44.8125);
        address.setLongitude(20.4612);
    }

    // 1) kreiranje posta preko multipart forme
    @Test
    @WithMockUser
    void createPostReturns201SavesThroughService() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "pic.jpg", "image/jpeg", "fake-image".getBytes(StandardCharsets.UTF_8));

        when(fileStorageService.storeFile(any())).thenReturn("uploads/images/pic.jpg");
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(locationService.cachePostLocation(any(Address.class))).thenReturn(address);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(123L);
            return p;
        });

        mockMvc.perform(multipart("/posts/create")
                        .file(image)
                        .param("userId", "7")
                        .param("description", "Hello OnlyBuns!")
                        .param("address", objectMapper.writeValueAsString(address))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Hello OnlyBuns!"))
                .andExpect(jsonPath("$.userId").value(7));

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        verify(locationService).cachePostLocation(any(Address.class));
    }

    // 2) get post, found (200)
    @Test
    @WithMockUser
    void getPostReturns200WhenFound() throws Exception {
        Post post = new Post();
        post.setId(5L);
        post.setDescription("desc");
        post.setUser(user);
        post.setLocation(address);
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));
        when(fileStorageService.getImage(anyString())).thenReturn("img".getBytes());

        mockMvc.perform(get("/posts/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.userId").value(7));
    }

    // 3) get post, not found (404)
    @Test
    @WithMockUser
    void getPostReturns404WhenNotFound() throws Exception {
        when(postRepository.findById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/posts/{id}", 404L))
                .andExpect(status().isNotFound());
    }

    // 4) get all posts, found (200)
    @Test
    void getAllPostsReturns200WithHeader() throws Exception {
        Post p1 = new Post(); p1.setId(1L); p1.setDescription("a"); p1.setUser(user); p1.setLocation(address);
        Post p2 = new Post(); p2.setId(2L); p2.setDescription("b"); p2.setUser(user); p2.setLocation(address);
        when(postRepository.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/posts/all"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Backend-Port"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", isIn(List.of(1,2))));
    }

    // 5) toggle like
    @Test
    void toggleLikeReturnsLikeStatus() throws Exception {
        long postId = 10L; long userId = 7L;
        Post post = new Post(); post.setId(postId); post.setUser(user);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findByIdForUpdate(postId)).thenReturn(Optional.of(post)); // koristi se u toggleLike
        when(likeRepository.findByUserAndPost(user, post)).thenReturn(Optional.empty()); // nije lajkovao -> like
        when(postRepository.findById(postId)).thenReturn(Optional.of(post)); // koristi se u getLikeStatus
        when(likeRepository.existsByUserIdAndPostId(userId, postId)).thenReturn(true);
        when(likeRepository.countByPost(post)).thenReturn(1L);

        mockMvc.perform(post("/posts/{postId}/toggle-like", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("userId", userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likesCount").value(1L));

        verify(likeRepository).save(any());
        verify(likeRepository, never()).delete(any());
    }

    // 6) delete post, 204 no content
    @Test
    void deletePostReturns204CallsDelete() throws Exception {
        doNothing().when(postRepository).deleteById(77L);

        mockMvc.perform(delete("/posts/{id}", 77L))
                .andExpect(status().isNoContent());

        verify(postRepository).deleteById(77L);
    }
}
