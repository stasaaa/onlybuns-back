package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.GroupChatMember;
import com.isa.onlybuns_back.model.GroupChat;
import com.isa.onlybuns_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupChatMemberRepository extends JpaRepository<GroupChatMember, Long> {
    Optional<GroupChatMember> findByGroupChatAndUser(GroupChat groupChat, User user);

    List<GroupChatMember> findByUser(User user);  // ovo ti treba
}
