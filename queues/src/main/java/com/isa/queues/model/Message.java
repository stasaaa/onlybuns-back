package com.isa.queues.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Message<T> {
    private String id;
    private T content;

    public Message(T content) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
    }
}
