package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.Following;
import com.isa.onlybuns_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowingRepository extends JpaRepository<Following, Long> {

    boolean existsByFollowerAndFollowed(User follower, User followed);

    void deleteByFollowerAndFollowed(User follower, User followed);

    List<Following> findAllByFollower(User follower);

    List<Following> findAllByFollowed(User followed);

    long countByFollowed(User followed);
    long countByFollower(User user);
}

