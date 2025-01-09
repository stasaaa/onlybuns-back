package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.model.queue.Message;
import com.isa.onlybuns_back.model.queue.MessageQueue;
import org.springframework.stereotype.Service;

@Service
public class MessageService<T> {
    private MessageQueue<T> queue = new MessageQueue<>();

    // Slanje poruke
    public void sendMessage(Message<T> message) {
        queue.sendMessage(message);
    }

    // Prijem poruke
    public Message<T> receiveMessage() {
        return queue.receiveMessage();
    }

    // Provera da li red ima poruka
    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }
}
