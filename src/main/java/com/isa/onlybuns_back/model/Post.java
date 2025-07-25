package com.isa.onlybuns_back.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @SequenceGenerator(
            name = "post_sequence",
            sequenceName = "post_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "post_sequence"
    )
    @Column(
            name = "post_id",
            updatable = false
    )
    private long id;

    @Column(
            name = "description",
            nullable = false
    )
    private String description;

    @Column(
            name = "image_path",
            nullable = false
    )
    private String imagePaths;

    @Column(
            name = "location",
            nullable = false
    )
    private Address location;

    @Column
    private Date creationTime;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @Column(name = "compressed", nullable = false)
    private boolean compressed = false;

    // Constructors
    public Post(Long id, String description, String image, Address location, Date creationTime, int likes, User user, List<Comment> comments) {
        this.id = id;
        this.description = description;
        this.imagePaths = image;
        this.location = location;
        this.creationTime = creationTime;
        this.user = user;
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

    public String getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(String image) {
        this.imagePaths = image;
    }

    public Address getLocation() {
        return location;
    }

    public void setLocation(Address location) {
        this.location = location;
    }

    public Date getCreationTime() { return creationTime; }

    public void setCreationTime(Date creationTime) { this.creationTime = creationTime; }

    // IZMENJENO: umesto int likes
    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }


    public int getLikesCount() {
        return likes == null ? 0 : likes.size();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public boolean getCompressed() { return compressed; }

    public void setCompressed(boolean compressed) { this.compressed = compressed; }
}
