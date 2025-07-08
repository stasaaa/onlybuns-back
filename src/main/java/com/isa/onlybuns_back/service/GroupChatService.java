package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.CreateGroupRequest;
import com.isa.onlybuns_back.dto.GroupChatSummaryDto;
import com.isa.onlybuns_back.dto.MessageDTO;
import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.model.GroupChat;
import com.isa.onlybuns_back.model.Message;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.GroupChatRepository;
import com.isa.onlybuns_back.repository.MessageRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRepository groupChatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;


    public GroupChat addMember(Long groupId, Long userId, String adminUsername) {
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User admin = userRepository.findByUsername(adminUsername);
        if (admin == null) {
            throw new IllegalArgumentException("Admin not found");
        }

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User to add not found"));

        if (group.getAdmin().getId() != admin.getId()) {
            throw new SecurityException("Only admin can perform this action.");
        }



        if (!group.getMembers().contains(userToAdd)) {
            group.getMembers().add(userToAdd);
            groupChatRepository.save(group);
            sendSystemMessage(group, userToAdd.getUsername() + " has been added.");
        }

        return group;
    }

    public GroupChat removeMember(Long groupId, Long userId, String adminUsername) {
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User admin = userRepository.findByUsername(adminUsername);
        if (admin == null) {
            throw new IllegalArgumentException("Admin not found");
        }

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User to remove not found"));

        if (group.getAdmin().getId() != admin.getId()) {
            throw new SecurityException("Only admin can perform this action.");
        }

        if (group.getMembers().remove(userToRemove)) {
            groupChatRepository.save(group);
            sendSystemMessage(group, userToRemove.getUsername() + " is removed from the group chat.");
        }

        return group;
    }

    private void sendSystemMessage(GroupChat group, String content) {
        Message message = new Message();
        message.setGroupChat(group);
        message.setSender(null);
        message.setTimestamp(new Date());
        message.setContent(content);
        messageRepository.save(message);

        MessageDTO messageDTO = new MessageDTO(
                content,
                "SYSTEM",
                message.getTimestamp()
        );

        messagingTemplate.convertAndSend("/topic/group/" + group.getId(), messageDTO);
    }


    public GroupChat createGroup(CreateGroupRequest request) {
        User admin = userRepository.findById(request.getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        List<User> members = userRepository.findAllById(request.getMemberIds());


        if (!members.contains(admin)) {
            members.add(admin);
        }

        GroupChat groupChat = new GroupChat();
        groupChat.setName(request.getGroupName());
        groupChat.setAdmin(admin);
        groupChat.setMembers(members);

        return groupChatRepository.save(groupChat);
    }



    public List<GroupChat> getGroupsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return groupChatRepository.findByMembersContaining(user);
    }

    public Message getLastMessageForGroup(Long groupId) {
        return messageRepository.findTopByGroupChatIdOrderByTimestampDesc(groupId).orElse(null);
    }

    public List<GroupChatSummaryDto> getGroupSummariesForUser(Long userId) {
        List<GroupChat> groups = getGroupsForUser(userId);
        return groups.stream().map(group -> {
            int memberCount = group.getMembers() != null ? group.getMembers().size() : 0;
            String adminUsername = group.getAdmin() != null ? group.getAdmin().getUsername() : null;

            Message lastMessage = getLastMessageForGroup(group.getId());

            GroupChatSummaryDto.LastMessageDTO lastMessageDTO = null;
            if (lastMessage != null) {
                lastMessageDTO = new GroupChatSummaryDto.LastMessageDTO(
                        lastMessage.getContent(),
                        lastMessage.getSender() != null ? lastMessage.getSender().getUsername() : "SYSTEM",
                        lastMessage.getTimestamp()
                );
            }

            return new GroupChatSummaryDto(
                    group.getId(),
                    group.getName(),
                    memberCount,
                    adminUsername,
                    lastMessageDTO
            );
        }).collect(Collectors.toList());
    }

    public List<Message> getLastMessages(Long groupId) {
        return messageRepository.findTop10ByGroupChatIdOrderByTimestampDesc(groupId);
    }

    public GroupChat getGroupById(Long groupId) {
        return groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    public List<UserDto> getGroupMembers(Long groupId) {
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id " + groupId));

        return group.getMembers().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

}
