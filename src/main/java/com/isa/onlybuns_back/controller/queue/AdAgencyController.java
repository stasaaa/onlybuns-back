package com.isa.onlybuns_back.controller.queue;

import com.isa.onlybuns_back.model.queue.AdMessage;
import com.isa.onlybuns_back.model.queue.Message;
import com.isa.onlybuns_back.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdAgencyController {
    @Autowired
    private MessageService<AdMessage> messageService;

    @GetMapping("/receiveAdMessage")
    public void receiveAdMessage() {
        Message<AdMessage> message = messageService.receiveMessage();
        if (message != null) {
            System.out.println("Received Ad Message: " + message.getId());
        }
    }
}
