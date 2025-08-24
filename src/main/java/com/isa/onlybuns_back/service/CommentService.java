package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.CommentDto;
import com.isa.onlybuns_back.image.FileStorageService;
import com.isa.onlybuns_back.model.Comment;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.CommentRepository;
import com.isa.onlybuns_back.repository.PostRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CommentService {
    private CommentRepository commentRepository;
    private UserRepository userRepository;
    private PostRepository postRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public List<CommentDto> getCommentsForPost(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreationTimeDesc(postId);
        return comments.stream().map(this::toDto).toList();
    }

    public CommentDto createComment(CommentDto commentDto) {
        Post post = postRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // checking whether the 60 comment restriction has been violated or not
        Date oneHourAgo = new Date(System.currentTimeMillis() - 3600_000);
        long recentCommentsCount = commentRepository.countRecentCommentsByUser(commentDto.getUserId(), oneHourAgo);
        if (recentCommentsCount >= 60) {
            throw new RuntimeException("Comment limit reached for this hour.");
        }

        User user = userRepository.findById(commentDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(commentDto.getContent());
        comment.setCreationTime(new Date());

        Comment saved = commentRepository.save(comment);
        return toDto(saved);
    }

    private CommentDto toDto(Comment c) {
        CommentDto dto = new CommentDto();
        dto.setId(c.getId());
        dto.setUserId(c.getUser().getId());
        dto.setPostId(c.getPost().getId());
        dto.setContent(c.getContent());
        dto.setCreationTime(c.getCreationTime());
        return dto;
    }

    private Comment toEntity(CommentDto dto) {
        Comment comment = new Comment();
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(dto.getContent());
        comment.setCreationTime(new Date());
        return comment;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Post post = comment.getPost();
        if (post != null) {
            post.getComments().remove(comment);  // ukloni komentar iz liste komentara posta
            comment.setPost(null);                // raskini vezu u comment entitetu
        }

        commentRepository.delete(comment);
    }

    public List<CommentDto> getCommentsForUser(Long userId) {
        return commentRepository.findByUserIdOrderByCreationTimeDesc(userId)
                .stream().map(this::toDto)
                .toList();
    }
}
