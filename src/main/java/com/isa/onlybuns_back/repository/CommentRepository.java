package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByCreationTimeAfter(Date fromDate);
}
