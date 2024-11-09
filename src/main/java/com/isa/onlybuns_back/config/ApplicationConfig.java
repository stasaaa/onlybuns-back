package com.isa.onlybuns_back.config;

import com.isa.onlybuns_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserDetails user = userRepository.findByUsername(username);
            if (user == null) {
                UserDetails userEmail = userRepository.findByEmail(username);
                if(userEmail != null) {
                    return userEmail;
                }
                throw new UsernameNotFoundException("User not found: " + username);
            }
            return user;
        };
    }
}
