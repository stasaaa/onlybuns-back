package com.isa.onlybuns_back.controller.queue;

import com.isa.onlybuns_back.model.Address;
import com.isa.onlybuns_back.model.queue.Message;
import com.isa.onlybuns_back.model.queue.RabbitCareMessage;
import com.isa.onlybuns_back.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RabbitCareController {
    @Autowired
    private MessageService<RabbitCareMessage> messageService;

    @PostMapping("/sendRabbitCareLocation")
    public void sendRabbitCareLocation(String locationName, Address locationAddress, String id) {
        RabbitCareMessage message = new RabbitCareMessage(locationName, locationAddress);
        messageService.sendMessage(new Message<RabbitCareMessage>(id, message));
    }
}
