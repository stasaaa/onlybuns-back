package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findTop10ByGroupChatIdOrderByTimestampDesc(Long groupId);

    Optional<Message> findTopByGroupChatIdOrderByTimestampDesc(Long groupId);
    List<Message> findTop10ByGroupChatIdAndTimestampBeforeOrderByTimestampDesc(Long groupChatId, Date before);

    List<Message> findByGroupChatIdAndTimestampAfterOrderByTimestampAsc(Long groupChatId, Date after);

}
