package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.Like;
import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Date;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndPost(User user, Post post);

    long countByPost(Post post);

    @Query("SELECT l.post FROM Like l WHERE l.user = :user")
    List<Post> findPostsLikedByUser(@Param("user") User user);
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.likedAt >= :since AND l.post.user = :user")
    long countLikesOnUsersPostsSince(@Param("user") User user, @Param("since") Date since);

}
