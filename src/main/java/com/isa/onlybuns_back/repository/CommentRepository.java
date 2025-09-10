package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.Comment;
import com.isa.onlybuns_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // gets all comments for post sorted from the newest to oldest
    List<Comment> findByPostIdOrderByCreationTimeDesc(Long postId);

    // counts the number of comments made by a user in the last hour (restriction: no more than 60 comments allowed)
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId AND c.creationTime > :oneHourAgo")
    long countRecentCommentsByUser(Long userId, Date oneHourAgo);

    List<Comment> findByCreationTimeAfter(Date date);

    List<Comment> findByUserIdOrderByCreationTimeDesc(Long userId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.user = :user AND c.creationTime >= :since")
    long countCommentsOnUsersPostsSince(@Param("user") User user, @Param("since") Date since);
}
