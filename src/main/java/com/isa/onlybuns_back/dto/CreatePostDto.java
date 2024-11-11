package com.isa.onlybuns_back.dto;

import com.isa.onlybuns_back.model.Address;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostDto {
    private long id;
    private String description;
    private MultipartFile image;
    private long userId;
    private Address address;
}