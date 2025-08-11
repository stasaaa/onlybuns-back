package com.isa.onlybuns_back.mapper;

import com.isa.onlybuns_back.dto.CommentDto;
import com.isa.onlybuns_back.dto.PostDto;
import com.isa.onlybuns_back.model.Post;

import java.util.stream.Collectors;
public class PostMapper {
    public static PostDto toDto(Post post, byte[] image, Long currentUserId) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setDescription(post.getDescription());
        dto.setUserId(post.getUser().getId());
        dto.setUsername(post.getUser().getUsername());
        dto.setAddress(post.getLocation());
        dto.setCreationTime(post.getCreationTime());
        dto.setLikes(post.getLikesCount());

        // Provera da li je lajkovan od strane currentUserId
        boolean liked = false;
        if (currentUserId != null) {
            liked = post.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId() == currentUserId.longValue());

        }
        dto.setLikedByCurrentUser(liked);

        dto.setComments(
                post.getComments().stream()
                        .map(comment -> {
                            CommentDto c = new CommentDto();
                            c.setId(comment.getId());
                            c.setUserId(comment.getUser().getId());
                            c.setContent(comment.getContent());
                            c.setCreationTime(comment.getCreationTime());
                            return c;
                        })
                        .collect(Collectors.toList())
        );

        return dto;
    }
}
