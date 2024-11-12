package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);

    @Query("SELECT p FROM Post p WHERE p.creationTime < :oneMonthAgo AND p.compressed = false")
    List<Post> findImagesToCompress(LocalDateTime oneMonthAgo);
}
