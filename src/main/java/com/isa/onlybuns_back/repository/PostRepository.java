package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);

    @Query("SELECT p FROM Post p WHERE p.creationTime < :oneMonthAgo AND p.compressed = false")
    List<Post> findImagesToCompress(LocalDate oneMonthAgo);

    List<Post> findByCreationTimeAfter(Date date);

    long count();

    @Query("SELECT COUNT(p) FROM Post p WHERE p.creationTime >= :startDate")
    long countPostsFromLastMonth(@Param("startDate") Date startDate);

//    TODO: Skroz promeniti ovo kada se lajkovi poprave
   // @Query("SELECT p FROM Post p WHERE p.creationTime >= :sevenDaysAgo ORDER BY p.likes DESC")
   // List<Post> getFiveMostLikedLastWeek(@Param("sevenDaysAgo") Date sevenDaysAgo, Pageable pageable);

  //  @Query("SELECT p FROM Post p ORDER BY p.likes DESC")
  //  List<Post> getTopTenMostLikedPosts(Pageable pageable);

  //  Page<Post> findAll(Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN p.likes l " +
            "WHERE p.creationTime >= :sevenDaysAgo " +
            "GROUP BY p " +
            "ORDER BY COUNT(l) DESC")
    List<Post> getFiveMostLikedLastWeek(@Param("sevenDaysAgo") Date sevenDaysAgo, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN p.likes l " +
            "GROUP BY p " +
            "ORDER BY COUNT(l) DESC")
    List<Post> getTopTenMostLikedPosts(Pageable pageable);
//ova dva querija mi je dao chatgpt - ja sam ih nalepila da ne bi bilo errora, feel free to change them

    @Query("SELECT p FROM Post p WHERE p.user.username = :username")
    Page<Post> findByUsernamePaged(@Param("username") String username, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user IN :authors AND p.creationTime >= :since")
    long countPostsByAuthorsSince(@Param("authors") List<User> authors, @Param("since") Date since);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findByIdForUpdate(@Param("id") Long id);

}
