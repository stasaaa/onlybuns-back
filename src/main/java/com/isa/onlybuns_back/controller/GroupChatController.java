package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.CreateGroupRequest;
import com.isa.onlybuns_back.dto.GroupChatSummaryDto;
import com.isa.onlybuns_back.dto.MessageDTO;
import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.model.GroupChat;
import com.isa.onlybuns_back.model.Message;
import com.isa.onlybuns_back.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("group-chat")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;

    @PostMapping
    public ResponseEntity<GroupChat> createGroup(@RequestBody CreateGroupRequest request) {
        GroupChat createdGroup = groupChatService.createGroup(request);
        return ResponseEntity.ok(createdGroup);
    }

    @GetMapping("/{groupId}/last-messages")
    public List<MessageDTO> getLastMessages(@PathVariable Long groupId) {
        List<Message> messages = groupChatService.getLastMessages(groupId);
        Collections.reverse(messages);

        return messages.stream()
                .map(msg -> new MessageDTO(
                        msg.getContent(),
                        msg.getSender() != null ? msg.getSender().getUsername() : "SYSTEM",
                        msg.getTimestamp()
                ))
                .collect(Collectors.toList());
    }


    @PutMapping("/{groupId}/add-member")
    public ResponseEntity<GroupChat> addMember(@PathVariable Long groupId,
                                               @RequestParam Long userId,
                                               @RequestParam String adminUsername) {
        GroupChat updatedGroup = groupChatService.addMember(groupId, userId, adminUsername);
        return ResponseEntity.ok(updatedGroup);
    }

    @PutMapping("/{groupId}/remove-member")
    public ResponseEntity<GroupChat> removeMember(@PathVariable Long groupId,
                                                  @RequestParam Long userId,
                                                  @RequestParam String adminUsername) {
        GroupChat updatedGroup = groupChatService.removeMember(groupId, userId, adminUsername);
        return ResponseEntity.ok(updatedGroup);
    }

    @GetMapping("/user/{userId}")
    public List<GroupChatSummaryDto> getGroupSummariesForUser(@PathVariable Long userId) {
        return groupChatService.getGroupSummariesForUser(userId);
    }

    @GetMapping("/{groupId}/members")
    public List<UserDto> getGroupMembers(@PathVariable Long groupId) {
        return groupChatService.getGroupMembers(groupId);
    }

    @GetMapping("/{groupId}/messages/user/{userId}")
    public List<MessageDTO> getMessagesForUser(@PathVariable Long groupId, @PathVariable Long userId) {
        List<Message> messages = groupChatService.getMessagesForUser(groupId, userId);
        return messages.stream()
                .map(msg -> new MessageDTO(
                        msg.getContent(),
                        msg.getSender() != null ? msg.getSender().getUsername() : "SYSTEM",
                        msg.getTimestamp()
                ))
                .collect(Collectors.toList());
    }


}
