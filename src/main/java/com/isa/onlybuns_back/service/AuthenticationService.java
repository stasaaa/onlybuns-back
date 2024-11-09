package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.repository.UserRepository;
import com.isa.onlybuns_back.mapper.UserMapper;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationService(UserRepository userRepository, UserMapper userMapper,
                                 JavaMailSender mailSender, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }


    public UserDto login(UserDto userDto){
        User user = userRepository.findByEmail(userDto.getEmail());

        if (user != null && user.getPassword().equals(userDto.getPassword())) {
            user.setLastLogin(new Date());
            return userMapper.userToUserDTO(user);
        }

        throw new IllegalArgumentException("Invalid email or password");
    }

    public boolean logout(UserDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail());

        if (user != null) {
            user.setLastLogin(new Date());
            return true;
        }
        return false;
    }

    public boolean register(UserDto userDto) {
        if (!userDto.getPassword().equals(userDto.getPasswordConfirm())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        if (userRepository.findByEmail(userDto.getEmail()) != null) {
            throw new IllegalArgumentException("Email address already in use.");
        }

        if(userRepository.findByUsername(userDto.getUsername()) != null) {
            throw new IllegalArgumentException("Username already in use.");
        }

        User user = userMapper.userDTOToUser(userDto);
        user.setActive(false);
        user.setUserRole(UserRole.REGISTERED);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        String token = UUID.randomUUID().toString();
        user.setActivationToken(token);

        userRepository.save(user);

//        sendActivationEmail(user.getEmail(), token);

        return true;
    }

    private void sendActivationEmail(String email, String token) {
        String activationLink = "http://localhost:8080/activate?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Activate your account");
        message.setText("Click the link to activate your account: " + activationLink);
        mailSender.send(message);
    }

    public boolean activateAccount(String token) {
        User user = userRepository.findByActivationToken(token);
        if (user != null && !user.isActive()) {
            user.setActive(true);
            user.setActivationToken(null);
            user.setLastLogin(new Date());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Collection<UserDto> getAll() {
        List<User> users = userRepository.findAll();
        return userMapper.usersToUserDTOs(users);
    }
}
