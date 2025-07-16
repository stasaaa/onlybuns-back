package com.isa.onlybuns_back.model;

import com.isa.onlybuns_back.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "followings", uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "followed_id"}))
@Getter
@Setter
public class Following {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne(optional = false)
    @JoinColumn(name = "followed_id")
    private User followed;

    private Date followedAt;

    public Following() {
        this.followedAt = new Date();
    }

    public Following(User follower, User followed) {
        this.follower = follower;
        this.followed = followed;
        this.followedAt = new Date();
    }
}
