package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class AuthenticationServiceTest {
    @Autowired
    private AuthenticationService authenticationService;

    @Test
    void testConcurrentRegistration() throws InterruptedException {
        UserDto userDto = new UserDto();
        userDto.setUsername("istiUsername");
        userDto.setPassword("istiPassword");
        userDto.setPasswordConfirm("istiPassword");
        userDto.setEmail("test@email.com");
        userDto.setFirstName("istiFirstName");
        userDto.setLastName("istiLastName");

        Runnable task = () -> {
            try {
                Thread.sleep(1000);
                authenticationService.register(userDto);
                System.out.println(Thread.currentThread().getName() + " SUCCESS" + LocalDateTime.now());
            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName() + " FAIL: " + LocalDateTime.now() + "   " + e.getMessage());
            }
        };

        Thread t1 = new Thread(task, "Thread-1");
        Thread t2 = new Thread(task, "Thread-2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }
}
