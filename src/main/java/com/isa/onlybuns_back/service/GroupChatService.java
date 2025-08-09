package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.CreateGroupRequest;
import com.isa.onlybuns_back.dto.GroupChatSummaryDto;
import com.isa.onlybuns_back.dto.MessageDTO;
import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.model.GroupChat;
import com.isa.onlybuns_back.model.GroupChatMember;
import com.isa.onlybuns_back.model.Message;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.GroupChatMemberRepository;
import com.isa.onlybuns_back.repository.GroupChatRepository;
import com.isa.onlybuns_back.repository.MessageRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRepository groupChatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GroupChatMemberRepository groupChatMemberRepository;

    public GroupChat addMember(Long groupId, Long userId, String adminUsername) {
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User admin = userRepository.findByUsername(adminUsername);
        if (admin == null) {
            throw new IllegalArgumentException("Admin not found");
        }

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User to add not found"));

        if (!Objects.equals(group.getAdmin().getId(), admin.getId())) {
            throw new SecurityException("Only admin can perform this action.");
        }

        boolean isAlreadyMember = groupChatMemberRepository.findByGroupChatAndUser(group, userToAdd).isPresent();

        if (!isAlreadyMember) {
            GroupChatMember newMember = new GroupChatMember();
            newMember.setGroupChat(group);
            newMember.setUser(userToAdd);
            newMember.setJoinedAt(new Date());
            groupChatMemberRepository.save(newMember);

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

        if (!Objects.equals(group.getAdmin().getId(), admin.getId())) {
            throw new SecurityException("Only admin can perform this action.");
        }

        // Pronađi GroupChatMember za korisnika koji treba da se ukloni
        groupChatMemberRepository.findByGroupChatAndUser(group, userToRemove)
                .ifPresent(groupChatMemberRepository::delete);

        sendSystemMessage(group, userToRemove.getUsername() + " is removed from the group chat.");

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

        GroupChat groupChat = new GroupChat();
        groupChat.setName(request.getGroupName());
        groupChat.setAdmin(admin);

        // Sačuvaj grupu prvo da dobije ID
        GroupChat savedGroupChat = groupChatRepository.save(groupChat);

        if (!members.contains(admin)) {
            members.add(admin);
        }

        List<GroupChatMember> groupMembers = new ArrayList<>();
        Date now = new Date();

        for (User user : members) {
            GroupChatMember member = new GroupChatMember();
            member.setGroupChat(savedGroupChat);
            member.setUser(user);
            member.setJoinedAt(now);
            groupMembers.add(member);
        }

        groupChatMemberRepository.saveAll(groupMembers);

        // Ne postavljaj savedGroupChat.setMembers(members) jer members su GroupChatMember, ne User
        // Ako treba, možeš postaviti listu GroupChatMember u savedGroupChat.setMembers(groupMembers);
        savedGroupChat.setMembers(groupMembers);

        return savedGroupChat;
    }

    public List<GroupChat> getGroupsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Preko repository-ja pronalazimo grupu gde je korisnik član (preko GroupChatMember)
        // Ova metoda treba da koristi GroupChatMemberRepository da traži po user-u ili da koristi custom query u GroupChatRepository
        // Pretpostavimo da postoji metoda u GroupChatMemberRepository:
        // List<GroupChatMember> findByUser(User user);

        List<GroupChatMember> memberships = groupChatMemberRepository.findByUser(user);

        // Izvući samo grupne chatove iz članstva
        return memberships.stream()
                .map(GroupChatMember::getGroupChat)
                .distinct()
                .collect(Collectors.toList());
    }

    public Message getLastMessageForGroup(Long groupId) {
        return messageRepository.findTopByGroupChatIdOrderByTimestampDesc(groupId).orElse(null);
    }

    public List<GroupChatSummaryDto> getGroupSummariesForUser(Long userId) {
        List<GroupChat> groups = getGroupsForUser(userId);
        return groups.stream().map(group -> {
            // Koristi broj članova preko GroupChatMember entiteta
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
                .map(GroupChatMember::getUser)  // Izvući User iz GroupChatMember
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    public List<Message> getMessagesForUser(Long groupId, Long userId) {
        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        GroupChatMember membership = groupChatMemberRepository.findByGroupChatAndUser(group, user)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of the group"));

        Date joinedAt = membership.getJoinedAt();

        List<Message> allMessagesAfterJoin = messageRepository.findByGroupChatIdAndTimestampAfterOrderByTimestampAsc(groupId, joinedAt);

        List<Message> last10BeforeJoin = messageRepository.findTop10ByGroupChatIdAndTimestampBeforeOrderByTimestampDesc(groupId, joinedAt);

        List<Message> combined = new ArrayList<>();
        last10BeforeJoin.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
        combined.addAll(last10BeforeJoin);
        combined.addAll(allMessagesAfterJoin);

        return combined;
    }
}
