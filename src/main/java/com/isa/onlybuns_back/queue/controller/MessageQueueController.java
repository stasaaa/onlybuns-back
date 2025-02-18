package com.isa.onlybuns_back.queue.controller;

import com.isa.onlybuns_back.queue.model.Message;
import com.isa.onlybuns_back.queue.model.RabbitCareLocation;
import com.isa.onlybuns_back.queue.service.MessageBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/queue")
public class MessageQueueController {

    private final MessageBroker messageBroker;

    @Autowired
    public MessageQueueController(MessageBroker messageBroker) {
        this.messageBroker = messageBroker;
    }

    // Endpoint to send RabbitCareLocation (direct)
    @PostMapping("/send/direct/{queueName}")
    public String sendRabbitCareLocation(@PathVariable String queueName, @RequestBody RabbitCareLocation location) {
        messageBroker.sendMessageToDirectQueue(queueName, location);
        return "Rabbit care location sent to " + queueName;
    }

    // Endpoint to receive RabbitCareLocation (direct)
    @GetMapping("/receive/direct/{queueName}")
    public Message<RabbitCareLocation> receiveRabbitCareLocation(@PathVariable String queueName) throws InterruptedException {
        return messageBroker.receiveMessageFromDirectQueue(queueName);
    }

    // Endpoint to broadcast advertisement to all agencies (fanout)
    @PostMapping("/broadcast/ad")
    public String broadcastAdvertisement(@RequestBody String message) {
        messageBroker.broadcastMessageToFanoutQueues(message);
        return "Advertisement broadcasted to all agencies";
    }

    // Endpoint to receive advertisement from a specific agency (fanout)
    @GetMapping("/receive/ad/{queueName}")
    public String receiveAdvertisement(@PathVariable String queueName) throws InterruptedException {
        return (String) messageBroker.receiveMessageFromFanoutQueue(queueName);
    }
}