package com.isa.onlybuns_back.model.queue;

import java.util.LinkedList;
import java.util.Queue;

public class MessageQueue<T> {
    private Queue<Message<T>> queue = new LinkedList<>();

    // Dodavanje poruke u red
    public void sendMessage(Message<T> message) {
        queue.offer(message);
    }

    // Uzimanje poruke iz reda
    public Message<T> receiveMessage() {
        return queue.poll();
    }

    // Provera da li je red prazan
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
