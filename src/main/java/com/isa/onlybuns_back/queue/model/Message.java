package com.isa.onlybuns_back.queue.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Message<T> {
    private String id;
    private T content;

    public Message(String id, T content) {
        this.id = id;
        this.content = content;
    }

}
