package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.irepository.IUserRepository;
import com.isa.onlybuns_back.iservice.IAuthenticationService;
import com.isa.onlybuns_back.mapper.UserMapper;
import com.isa.onlybuns_back.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthenticationService implements IAuthenticationService {
    private final IUserRepository userRepository;
    private final UserMapper userMapper;
    private final JavaMailSender mailSender;

    @Autowired
    public AuthenticationService(IUserRepository userRepository, UserMapper userMapper, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.mailSender = mailSender;
    }

    @Override
    public boolean login(UserDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail());

        if (user != null && user.getPassword().equals(userDto.getPassword())) {
            user.setLastLogin(new Date());
            return true;
        }
        return false;
    }

    @Override
    public boolean logout(UserDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail());

        if (user != null) {
            user.setLastLogin(new Date());
            return true;
        }
        return false;
    }

    @Override
    public boolean register(UserDto userDto) {
        if (!userDto.getPassword().equals(userDto.getPasswordConfirm())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = userMapper.userDTOToUser(userDto);
        user.setActive(false);

        String token = UUID.randomUUID().toString();
        user.setActivationToken(token);

        userRepository.save(user);

        sendActivationEmail(user.getEmail(), token);

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

    @Override
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

    @Override
    public Collection<UserDto> getAll() {
        List<User> users = userRepository.findAll();
        return userMapper.usersToUserDTOs(users);
    }
}
