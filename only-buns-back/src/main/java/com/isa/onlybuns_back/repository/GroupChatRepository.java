package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.GroupChat;
import com.isa.onlybuns_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    List<GroupChat> findByMembersContaining(User user);
    @Query("SELECT g FROM GroupChat g LEFT JOIN FETCH g.members WHERE g.id = :groupId")
    Optional<GroupChat> findByIdWithMembers(@Param("groupId") Long groupId);
}
