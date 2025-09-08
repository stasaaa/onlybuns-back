package com.isa.queues.model;

import java.util.LinkedList;
import java.util.Queue;

public class SimpleQueue<T> {
    private final Queue<Message<T>> queue = new LinkedList<>();

    public synchronized void send(T messageContent) {
        Message<T> message = new Message<>(messageContent);
        queue.offer(message);
        notifyAll();
    }

    public synchronized Message<T> receive() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
