package com.isa.onlybuns_back.queue.service;

import com.isa.onlybuns_back.queue.model.SimpleQueue;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageBroker {
    private final Map<String, SimpleQueue<Object>> directQueues = new HashMap<>();  // Direct queues (for Rabbit Care Locations)
    private final Map<String, SimpleQueue<Object>> fanoutQueues = new HashMap<>();  // Fanout queues (for Advertising Agencies)

    // Initialize queues
    public MessageBroker() {
        // Create some default queues
        createDirectQueue("rabbit-care");
        createFanoutQueue("ad-agency1");
        createFanoutQueue("ad-agency2");
    }

    // Create Direct Queue
    public void createDirectQueue(String queueName) {
        directQueues.put(queueName, new SimpleQueue<>());
    }

    // Create Fanout Queue
    public void createFanoutQueue(String queueName) {
        fanoutQueues.put(queueName, new SimpleQueue<>());
    }

    // Send a message to a specific queue (direct)
    public <T> void sendMessageToDirectQueue(String queueName, T message) {
        SimpleQueue<T> queue = (SimpleQueue<T>) directQueues.get(queueName);
        if (queue != null) {
            queue.send(message);
        }
    }

    // Broadcast a message to all fanout queues
    public <T> void broadcastMessageToFanoutQueues(T message) {
        for (SimpleQueue<Object> queue : fanoutQueues.values()) {
            queue.send(message);
        }
    }

    // Receive a message from a direct queue
    public <T> T receiveMessageFromDirectQueue(String queueName) throws InterruptedException {
        SimpleQueue<T> queue = (SimpleQueue<T>) directQueues.get(queueName);
        if (queue != null && !queue.isEmpty()) {
            return (T) queue.receive();
        }
        return null;
    }

    // Receive a message from a fanout queue
    public <T> T receiveMessageFromFanoutQueue(String queueName) throws InterruptedException {
        SimpleQueue<T> queue = (SimpleQueue<T>) fanoutQueues.get(queueName);
        if (queue != null && !queue.isEmpty()) {
            return (T) queue.receive();
        }
        return null;
    }

    // Generate unique ID for RabbitCareLocation
    public String generateMessageId() {
        return UUID.randomUUID().toString();
    }
}