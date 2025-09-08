package com.isa.queues.model;

import java.util.Map;

public abstract class Exchange {
    protected final String name;
    protected final Map<String, SimpleQueue> queues;

    public Exchange(String name, Map<String, SimpleQueue> queues) {
        this.name = name;
        this.queues = queues;
    }

    public abstract void bindQueue(String key, String queueName);
    public abstract void sendMessage(String key, Object message);

    public String getName() {
        return name;
    }
}
