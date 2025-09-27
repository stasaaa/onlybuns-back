package com.isa.onlybuns_back.standardizacija.unit;

import com.isa.onlybuns_back.service.PostService;
import com.isa.onlybuns_back.service.LocationService;
import com.isa.onlybuns_back.dto.PostDto;
import com.isa.onlybuns_back.image.FileStorageService;
import com.isa.onlybuns_back.model.Address;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.LikeRepository;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostCreateAndUpdateTest {

    @Mock PostRepository postRepository;
    @Mock UserRepository userRepository;
    @Mock FileStorageService fileStorageService;
    @Mock LikeRepository likeRepository;
    @Mock LocationService locationService;

    private PostService postService;

    @BeforeEach
    void setup() {
        postService = new PostService(
                postRepository,
                userRepository,
                fileStorageService,
                likeRepository,
                locationService
        );
    }

    // 1) empty description -> baca RuntimeException("Description cannot be empty.")
    @Test
    void postCannotBeCreatedWithEmptyDescription() throws IOException {
        PostDto dto = new PostDto();
        dto.setUserId(1L);
        dto.setDescription("   ");
        dto.setAddress(new Address());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> postService.create(dto, "uploads/images/img.jpg"));

        assertTrue(ex.getMessage().contains("Description cannot be empty."));
        verifyNoInteractions(userRepository, postRepository, locationService);
    }

    // 2) description > 250 -> baca RuntimeException("Description cannot exceed 250 characters.")
    @Test
    void postCannotBeCreatedWithDescOver250Characters() throws IOException {
        PostDto dto = new PostDto();
        dto.setUserId(1L);
        dto.setDescription("x".repeat(251));
        dto.setAddress(new Address());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> postService.create(dto, "uploads/images/img.jpg"));

        assertTrue(ex.getMessage().contains("Description cannot exceed 250 characters."));
        verifyNoInteractions(userRepository, postRepository, locationService);
    }

    // 3) valid description, user not found -> vraca null i ne radi save
    @Test
    void postCannotBeCreatedIfUserIsNotFound() throws IOException {
        long missingUserId = 42L;
        PostDto dto = new PostDto();
        dto.setUserId(missingUserId);
        dto.setDescription("valid");
        dto.setAddress(new Address());

        when(userRepository.findById(missingUserId)).thenReturn(Optional.empty());

        PostDto result = postService.create(dto, "uploads/images/img.jpg");

        assertNull(result, "Ocekuje se null kada korisnik ne postoji.");
        verify(userRepository).findById(missingUserId);
        verify(postRepository, never()).save(any(Post.class));
        verifyNoInteractions(locationService);
    }

    // 4) user postoji, post sacuvan, vraca dto
    @Test
    void postIsCreatedWhenUserIsValidAndDescriptionIsNotEmpty() throws IOException {
        long userId = 7L;
        String description = "OnlyBuns!";
        String imagePath = "uploads/images/img.jpg";

        PostDto dto = new PostDto();
        dto.setUserId(userId);
        dto.setDescription(description);

        Address inputAddress = new Address();
        inputAddress.setLatitude(44.8125);
        inputAddress.setLongitude(20.4612);
        dto.setAddress(inputAddress);

        User user = new User();
        user.setId(userId);

        Address cachedAddress = new Address();
        cachedAddress.setLatitude(44.813);
        cachedAddress.setLongitude(20.461);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(locationService.cachePostLocation(inputAddress)).thenReturn(cachedAddress);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0, Post.class);
            p.setId(123L);
            return p;
        });

        PostDto result = postService.create(dto, imagePath);

        assertSame(dto, result, "Metod vraca prosledjeni DTO.");
        verify(userRepository).findById(userId);
        verify(locationService).cachePostLocation(inputAddress);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post saved = postCaptor.getValue();

        assertEquals(description, saved.getDescription());
        assertEquals(user, saved.getUser());
        assertEquals(imagePath, saved.getImagePaths());
        assertEquals(cachedAddress, saved.getLocation());
        assertNotNull(saved.getCreationTime(), "creationTime se postavlja pre snimanja.");
        assertTrue(saved.getComments() instanceof ArrayList<?>);
    }

    // 5) update kada post ne postoji -> Optional.empty()
    @Test
    void postIsNotUpdatedIfPostIsNotFound() {
        long postId = 123L;
        PostDto dto = new PostDto();
        dto.setDescription("new desc");

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        Optional<PostDto> result = postService.update(postId, dto);

        assertTrue(result.isEmpty(), "Ocekuje se prazan Optional kada post ne postoji.");
        verify(postRepository).findById(postId);
        verify(postRepository, never()).save(any());
    }

    // 6) update kada post postoji -> setuje se description, sacuva i vraca Optional.of(dto)
    @Test
    void postDescriptionIsUpdatedIfPostIsFound() {
        long postId = 5L;

        Post existing = new Post();
        existing.setId(postId);
        existing.setDescription("old desc");

        PostDto dto = new PostDto();
        dto.setDescription("new desc");

        when(postRepository.findById(postId)).thenReturn(Optional.of(existing));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<PostDto> result = postService.update(postId, dto);

        assertTrue(result.isPresent(), "Treba da vrati Optional sa DTO.");
        assertEquals("new desc", existing.getDescription(), "Post.description mora biti azuriran.");
        assertEquals("new desc", result.get().getDescription(), "Vraceni DTO treba da ima novi opis.");

        verify(postRepository).findById(postId);
        verify(postRepository).save(existing);
    }
}