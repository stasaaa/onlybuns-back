package com.isa.onlybuns_back.irepository;

import com.isa.onlybuns_back.model.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    User findByActivationToken(String token);

    User findByEmail(String email);

    User findByUsername(String username);
}
