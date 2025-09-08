package com.isa.onlybuns_back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 250, message = "Comment cannot exceed 250 characters.")
    private String content;

    private Date creationTime;



}
