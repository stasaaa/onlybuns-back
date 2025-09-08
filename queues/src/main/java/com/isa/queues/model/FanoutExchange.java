package com.isa.queues.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FanoutExchange extends Exchange{
    private final List<String> boundQueueNames = new ArrayList<>();

    public FanoutExchange(String name, Map<String, SimpleQueue> queues) {
        super(name, queues);
    }

    @Override
    public void bindQueue(String key, String queueName) {
        if (queues.containsKey(queueName) && !boundQueueNames.contains(queueName)) {
            boundQueueNames.add(queueName);
        }
    }

    @Override
    public void sendMessage(String key, Object message) {
        for (String queueName : boundQueueNames) {
            queues.get(queueName).send(message);
            System.out.printf("Fanout Exchange '%s': Poruka poslata u red '%s'.\n", name, queueName);
        }
    }
}
