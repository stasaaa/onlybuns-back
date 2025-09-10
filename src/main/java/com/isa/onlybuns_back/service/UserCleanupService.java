package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCleanupService {

    private final UserRepository userRepository;

    // Cron for the last day in the month
  //   @Scheduled(cron = "0 */1 * * * *")
    @Scheduled(cron = "0 59 23 L * ?")
    public void deleteInactiveUsers() {
        userRepository.deleteAllInactiveUsers();
        System.out.println("All inactive users deleted");
    }
}
