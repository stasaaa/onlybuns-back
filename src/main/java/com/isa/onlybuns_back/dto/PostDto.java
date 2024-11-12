package com.isa.onlybuns_back.dto;

import com.isa.onlybuns_back.model.Address;
import com.isa.onlybuns_back.model.Post;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private long id;
    private String description;
    private byte[] image; // Use MultipartFile for file uploads
    private long userId;
    private Address address;
    private Date creationTime;
    private int likes;
    private String username;

}