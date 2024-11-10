package com.isa.onlybuns_back.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private int id;
    private String description;
    private List<byte[]> images;
    private long userId;
}
