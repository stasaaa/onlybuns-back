package com.isa.onlybuns_back.standardizacija.unit;

import com.isa.onlybuns_back.dto.CommentDto;
import com.isa.onlybuns_back.model.Comment;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.CommentRepository;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import com.isa.onlybuns_back.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentCreateAndDeleteTest {

    @Mock CommentRepository commentRepository;
    @Mock UserRepository userRepository;
    @Mock PostRepository postRepository;

    @InjectMocks CommentService commentService;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(4L);

        post = new Post();
        post.setId(103L);
    }

    // 1) uspesno kreiranje komentara
    @Test
    void createComment_success() {
        CommentDto input = new CommentDto();
        input.setUserId(4L);
        input.setPostId(103L);
        input.setContent("yaay");

        when(postRepository.findById(103L)).thenReturn(Optional.of(post));
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(commentRepository.countRecentCommentsByUser(eq(4L), any(Date.class))).thenReturn(0L);

        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(351L);
            c.setCreationTime(new Date());
            return c;
        });

        CommentDto result = commentService.createComment(input);

        assertThat(result.getId()).isEqualTo(351L);
        assertThat(result.getUserId()).isEqualTo(4L);
        assertThat(result.getPostId()).isEqualTo(103L);
        assertThat(result.getContent()).isEqualTo("yaay");
        verify(postRepository).findById(103L);
        verify(userRepository).findById(4L);
        verify(commentRepository).countRecentCommentsByUser(eq(4L), any(Date.class));
        verify(commentRepository).save(any(Comment.class));
    }

    // 2) post ne postoji, neuspesno dodavanje komentara
    @Test
    void createComment_postNotFound() {
        CommentDto input = new CommentDto();
        input.setUserId(4L);
        input.setPostId(999L);
        input.setContent("test");

        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> commentService.createComment(input));

        assertThat(ex.getMessage()).isEqualTo("Post not found");
        verify(postRepository).findById(999L);
        verify(userRepository, never()).findById(any());
        verify(commentRepository, never()).countRecentCommentsByUser(any(), any());
        verify(commentRepository, never()).save(any());
    }

    // 3) user ne postoji, neuspesno dodavanje komentara
    @Test
    void createComment_userNotFound() {
        CommentDto input = new CommentDto();
        input.setUserId(404L);
        input.setPostId(103L);
        input.setContent("test");

        when(postRepository.findById(103L)).thenReturn(Optional.of(post));
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> commentService.createComment(input));

        assertThat(ex.getMessage()).isEqualTo("User not found");
        verify(postRepository).findById(103L);
        verify(userRepository).findById(404L);
        verify(commentRepository, never()).save(any());
    }

    // 4) predjen je limit od 60 komentara u satu, neuspesno dodavanje komentara
    @Test
    void createComment_limitExceeded() {
        CommentDto input = new CommentDto();
        input.setUserId(4L);
        input.setPostId(103L);
        input.setContent("test");

        when(postRepository.findById(103L)).thenReturn(Optional.of(post));
        when(commentRepository.countRecentCommentsByUser(eq(4L), any(Date.class)))
                .thenReturn(60L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> commentService.createComment(input));

        assertThat(ex.getMessage()).isEqualTo("Comment limit reached for this hour.");

        verify(postRepository).findById(103L);
        verify(commentRepository).countRecentCommentsByUser(eq(4L), any(Date.class));
        verify(commentRepository, never()).save(any());
        verify(userRepository, never()).findById(any()); // user se ne poziva jer se prekida na limit proveri
    }

    // 5) uspesno brisanje komentara
    @Test
    void deleteComment_success() {
        Post post = new Post();
        post.setComments(new ArrayList<>());

        Comment comment = new Comment();
        comment.setId(351L);
        comment.setUser(user);
        comment.setPost(post);

        post.getComments().add(comment); // simulira da je komentar u listi

        when(commentRepository.findById(351L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(351L);

        assertThat(comment.getPost()).isNull(); // veza raskinuta
        assertThat(post.getComments()).doesNotContain(comment); // obrisan iz liste
        verify(commentRepository).findById(351L);
        verify(commentRepository, times(1)).delete(comment);
    }
}
