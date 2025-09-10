package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Date;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

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

    //pesimistic lock for following
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.username = :username")
    User findWithLockingByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE " +
            "(:searchQuery IS NULL OR " +
            "LOWER(CAST(u.firstName AS string)) LIKE LOWER(CONCAT('%', CAST(:searchQuery AS string), '%')) OR " +
            "LOWER(CAST(u.lastName AS string)) LIKE LOWER(CONCAT('%', CAST(:searchQuery AS string), '%')) OR " +
            "LOWER(CAST(u.email AS string)) LIKE LOWER(CONCAT('%', CAST(:searchQuery AS string), '%'))) AND " +
            "(:minPosts IS NULL OR SIZE(u.posts) >= :minPosts) AND " +
            "(:maxPosts IS NULL OR SIZE(u.posts) <= :maxPosts)")
    Page<User> findFiltered(@Param("searchQuery") String searchQuery,
                            @Param("minPosts") Integer minPosts,
                            @Param("maxPosts") Integer maxPosts,
                            Pageable pageable);



    @Query("SELECT COUNT(f) FROM Following f WHERE f.follower.id = :userId")
    long countFollowingByUserId(@Param("userId") Long userId);

    @Query("SELECT u FROM User u")
    List<User> findAllSimple();

    @Query("SELECT u FROM User u WHERE u.id <> :currentUserId")
    List<User> findAllExceptCurrent(@Param("currentUserId") Long currentUserId);

    @Query("SELECT u FROM User u " +
            "WHERE u.lastLogin < :inactiveBefore " +
            "AND (u.lastSummarySentAt IS NULL OR u.lastSummarySentAt < :notifyBefore)")
    List<User> findInactiveUsersForSummary(@Param("inactiveBefore") Date inactiveBefore,
                                           @Param("notifyBefore") Date notifyBefore);

}
