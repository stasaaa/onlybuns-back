package com.isa.queues.controller;

import com.isa.queues.model.AdMessage;
import com.isa.queues.model.Message;
import com.isa.queues.model.RabbitLocationMessage;
import com.isa.queues.service.MessageBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/queue")
public class MessageQueueController {

    private final MessageBroker messageBroker;

    @Autowired
    public MessageQueueController(MessageBroker messageBroker) {
        this.messageBroker = messageBroker;
    }

    @PostMapping("/send/direct/{exchangeName}/{routingKey}")
    public ResponseEntity<String> sendRabbitLocation(@PathVariable String exchangeName, @PathVariable String routingKey, @RequestBody RabbitLocationMessage location) {
        messageBroker.sendMessageToExchange(exchangeName, routingKey, location);
        return new ResponseEntity<>("Lokacija poslata u exchange: " + exchangeName + " sa ključem: " + routingKey, HttpStatus.OK);
    }

    @PostMapping("/send/fanout/{exchangeName}")
    public ResponseEntity<String> sendAdMessage(@PathVariable String exchangeName, @RequestBody AdMessage adMessage) {
        messageBroker.sendMessageToExchange(exchangeName, null, adMessage);
        return new ResponseEntity<>("Oglas poslat u fanout exchange: " + exchangeName, HttpStatus.OK);
    }

    @GetMapping("/receive/{queueName}")
    public ResponseEntity<?> receiveMessage(@PathVariable String queueName) throws InterruptedException {
        Message<?> receivedMessage = messageBroker.receiveMessageFromQueue(queueName);
        if (receivedMessage != null) {
            return new ResponseEntity<>(receivedMessage.getContent(), HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }
}