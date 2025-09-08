package com.isa.onlybuns_back.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class GroupChatSummaryDto {
    private Long id;
    private String name;
    private int memberCount;
    private String adminUsername;
    private LastMessageDTO lastMessage;

    public GroupChatSummaryDto(Long id, String name, int memberCount, String adminUsername, LastMessageDTO lastMessage) {
        this.id = id;
        this.name = name;
        this.memberCount = memberCount;
        this.adminUsername = adminUsername;
        this.lastMessage = lastMessage;
    }

    public static class LastMessageDTO {
        private String content;
        private String senderUsername;
        private LocalDateTime timestamp;

        public LastMessageDTO(String content, String senderUsername, Date timestamp) {
            this.content = content;
            this.senderUsername = senderUsername;
            this.timestamp = timestamp != null ? LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault()) : null;
        }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getSenderUsername() { return senderUsername; }
        public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    // Getters i setters za spoljašnje polja
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }

    public LastMessageDTO getLastMessage() { return lastMessage; }
    public void setLastMessage(LastMessageDTO lastMessage) { this.lastMessage = lastMessage; }
}
