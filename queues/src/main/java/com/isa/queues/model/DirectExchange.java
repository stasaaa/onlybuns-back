package com.isa.queues.model;

import java.util.HashMap;
import java.util.Map;

public class DirectExchange extends Exchange {
    private final Map<String, String> bindings = new HashMap<>();

    public DirectExchange(String name, Map<String, SimpleQueue> queues) {
        super(name, queues);
    }

    @Override
    public void bindQueue(String key, String queueName) {
        bindings.put(key, queueName);
    }

    @Override
    public void sendMessage(String key, Object message) {
        String queueName = bindings.get(key);
        if (queueName != null && queues.containsKey(queueName)) {
            queues.get(queueName).send(message);
            System.out.printf("Direct Exchange '%s': Poruka rutirana ključem '%s' u red '%s'.\n", name, key, queueName);
        } else {
            System.err.printf("Direct Exchange '%s': Nema reda povezanog sa ključem '%s'. Poruka je odbačena.\n", name, key);
        }
    }
}
