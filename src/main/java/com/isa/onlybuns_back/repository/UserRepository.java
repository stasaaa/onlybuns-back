package com.isa.onlybuns_back.repository;

import com.isa.onlybuns_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByActivationToken(String token);

    User findByEmail(String email);

    User findByUsername(String username);
}
