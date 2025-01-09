package com.isa.onlybuns_back.model.queue;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message<T> {
    private String id;
    private T content;
}