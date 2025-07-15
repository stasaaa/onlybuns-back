package com.isa.onlybuns_back.queue.model;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class SimpleQueue<T> {
    private final Queue<Message<T>> queue = new LinkedList<>();

    public void send(T messageContent) {
        // Generate a unique ID for each message
        String messageId = UUID.randomUUID().toString();
        Message<T> message = new Message<>(messageId, messageContent);
        queue.offer(message);
    }

    public Message<T> receive() throws InterruptedException {
        // Wait until the message is available in the queue
        while (queue.isEmpty()) {
            Thread.sleep(100);
        }
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}