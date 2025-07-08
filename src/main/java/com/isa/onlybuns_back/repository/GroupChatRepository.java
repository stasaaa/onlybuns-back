package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.GroupChat;
import com.isa.onlybuns_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    List<GroupChat> findByMembersContaining(User user);

}
