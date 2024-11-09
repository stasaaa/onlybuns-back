package com.isa.onlybuns_back.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column
    private String image;

    @Column
    private String location; // If you add a Location class, replace this with a Location object

    @Column
    private int likes;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "post_id") // Foreign key to link comments with the post
    private List<Comment> comments;

    // Constructors
    public Post(Long id, String description, String image, String location, int likes, long userId, List<Comment> comments) {
        this.id = id;
        this.description = description;
        this.image = image;
        this.location = location;
        this.likes = likes;
        this.userId = userId;
        this.comments = comments;
    }

    public Post() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
