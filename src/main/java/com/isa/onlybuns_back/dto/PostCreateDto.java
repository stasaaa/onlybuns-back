package com.isa.onlybuns_back.dto;

public class PostCreateDto {
    private String description;
    private String image;
    private String location;
    private Long userId;

    // Default constructor
    public PostCreateDto() {
    }

    // Constructor with all fields
    public PostCreateDto(String description, String image, String location, Long userId) {
        this.description = description;
        this.image = image;
        this.location = location;
        this.userId = userId;
    }

    // Getters and Setters
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "PostCreateDTO{" +
                "description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", location='" + location + '\'' +
                ", userId=" + userId +
                '}';
    }
}