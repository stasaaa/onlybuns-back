package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findTop10ByGroupChatIdOrderByTimestampDesc(Long groupId);

    Optional<Message> findTopByGroupChatIdOrderByTimestampDesc(Long groupId);
}
