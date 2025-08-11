package com.isa.onlybuns_back.controller;
import java.util.Optional;
import com.isa.onlybuns_back.dto.MessageDTO;
import com.isa.onlybuns_back.model.GroupChat;
import com.isa.onlybuns_back.model.Message;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.GroupChatRepository;
import com.isa.onlybuns_back.repository.MessageRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Date;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GroupChatRepository groupChatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @MessageMapping("/chat/{groupId}")
    public void sendMessage(@DestinationVariable Long groupId, MessageDTO messageDTO) {
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        String username = messageDTO.getSenderUsername();
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByUsername(username));

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Sender not found");
        }

        User sender = optionalUser.get();

        Message message = new Message();
        message.setContent(messageDTO.getContent());
        message.setTimestamp(new Date());
        message.setSender(sender);
        message.setGroupChat(group);

       messageRepository.save(message);

        messagingTemplate.convertAndSend("/topic/group/" + groupId, messageDTO);

    }
}
