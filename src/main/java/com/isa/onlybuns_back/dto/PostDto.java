package com.isa.onlybuns_back.dto;

import com.isa.onlybuns_back.model.Address;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
    private long id;
    private String description;
    private long userId;
    private Address address;
    private Date creationTime;
    private int likes;
    private String username;
    private List<CommentDto> comments;
    private boolean likedByCurrentUser;

    // getter i setter
    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }
}
