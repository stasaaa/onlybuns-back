package com.isa.onlybuns_back.irepository;

import com.isa.onlybuns_back.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);

}
