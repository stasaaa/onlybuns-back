package com.isa.onlybuns_back.model;

import jakarta.persistence.*;
import java.util.Date;
@Entity
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
public class Like {

    @Id
    @SequenceGenerator(
            name = "like_sequence",
            sequenceName = "like_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "like_sequence"
    )
    @Column(name = "like_id", updatable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "liked_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date likedAt = new Date();

    protected Like() {}

    public Like(User user, Post post) {
        this.user = user;
        this.post = post;
        this.likedAt = new Date();
    }

    // Getteri i setteri
    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public Date getLikedAt() { return likedAt; }
    public void setLikedAt(Date likedAt) { this.likedAt = likedAt; }
}
