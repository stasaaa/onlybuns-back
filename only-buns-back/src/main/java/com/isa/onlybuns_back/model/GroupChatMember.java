package com.isa.onlybuns_back.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class GroupChatMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private GroupChat groupChat;

    @ManyToOne
    private User user;

    private boolean isAdmin;

    private Date joinedAt;
}
