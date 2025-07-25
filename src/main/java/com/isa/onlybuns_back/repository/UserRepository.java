package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.Date;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByActivationToken(String token);

    User findByEmail(String email);

    User findByUsername(String username);
    List<User> findByLastLoginBefore(Date date);

    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.isActive = false")
    void deleteAllInactiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE SIZE(u.posts) > 0")
    long countUsersWithPosts();

    @Query("SELECT COUNT(u) FROM User u WHERE SIZE(u.posts) = 0 AND SIZE(u.comments) > 0")
    long countUsersWithOnlyComments();



}
