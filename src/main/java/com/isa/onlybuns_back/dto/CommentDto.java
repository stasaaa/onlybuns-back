package com.isa.onlybuns_back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CommentDto {
    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    private Long postId;

    @NotBlank
    private String content;

    private Date creationTime;



}
