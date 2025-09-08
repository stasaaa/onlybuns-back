package com.isa.queues.service;

import com.isa.queues.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class MessageBroker {
    private final Map<String, SimpleQueue> queues = new HashMap<>();
    private final Map<String, Exchange> exchanges = new HashMap<>();

    @PostConstruct
    public void init() {
        createQueue("rabbit-care-queue");
        createQueue("ad-agency1-queue");
        createQueue("ad-agency2-queue");

        DirectExchange directExchange = new DirectExchange("rabbit-care-exchange", queues);
        directExchange.bindQueue("location.update", "rabbit-care-queue");
        exchanges.put("rabbit-care-exchange", directExchange);

        FanoutExchange fanoutExchange = new FanoutExchange("ad-broadcast-exchange", queues);
        fanoutExchange.bindQueue(null, "ad-agency1-queue");
        fanoutExchange.bindQueue(null, "ad-agency2-queue");
        exchanges.put("ad-broadcast-exchange", fanoutExchange);

        System.out.println("Message Broker inicijalizovan. Kreirani su redovi i exchange-i.");
    }

    public void createQueue(String queueName) {
        queues.putIfAbsent(queueName, new SimpleQueue());
    }

    public void sendMessageToExchange(String exchangeName, String routingKey, Object message) {
        Exchange exchange = exchanges.get(exchangeName);
        if (exchange != null) {
            exchange.sendMessage(routingKey, message);
        } else {
            System.err.println("Exchange ne postoji: " + exchangeName);
        }
    }

    public <T> Message<T> receiveMessageFromQueue(String queueName) throws InterruptedException {
        SimpleQueue<T> queue = (SimpleQueue<T>) queues.get(queueName);
        if (queue != null) {
            return queue.receive();
        }
        return null;
    }
}