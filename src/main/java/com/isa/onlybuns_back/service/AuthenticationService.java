package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.AuthenticationRequest;
import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.dto.AuthenticationResponse;
import com.isa.onlybuns_back.mapper.AddressMapper;
import com.isa.onlybuns_back.repository.UserRepository;
import com.isa.onlybuns_back.mapper.UserMapper;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.model.UserRole;
import com.isa.onlybuns_back.security.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationService(UserRepository userRepository, UserMapper userMapper, AddressMapper addressMapper,
                                 JavaMailSender mailSender, PasswordEncoder passwordEncoder,
                                 JWTService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public UserDto userDetails(String email) {
        User user = userRepository.findByEmail(email);
        UserDto userdto = new UserDto();
        userdto.setId(user.getId());
        userdto.setUsername(user.getUsername());
        userdto.setEmail(user.getEmail());
        userdto.setFirstName(user.getFirstName());
        userdto.setLastName(user.getLastName());
        userdto.setActive(user.isActive());
        userdto.setAddress(addressMapper.addressToAddressDto(user.getAddress()));
        userdto.setUserRole(user.getUserRole());
        return userdto;
    }

    public AuthenticationResponse login(AuthenticationRequest authenticationRequest){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getEmail(), authenticationRequest.getPassword()));
        var user = userRepository.findByEmail(authenticationRequest.getEmail());
        if(!user.isActive()){
            throw new IllegalArgumentException("Account is not active");
        }
        user.setLastLogin(new Date());
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return  AuthenticationResponse
                .builder()
                .token(jwtToken)
                .build();
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

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.getAddress().setCountry(userDto.getAddress().getCountry());
        user.getAddress().setCity(userDto.getAddress().getCity());
        user.getAddress().setPostalCode(userDto.getAddress().getPostalCode());
        user.getAddress().setStreet(userDto.getAddress().getStreet());
        user.getAddress().setNumber(userDto.getAddress().getNumber());
        user.setActive(false);
        user.setUserRole(UserRole.REGISTERED);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        String token = UUID.randomUUID().toString();
        user.setActivationToken(token);

        userRepository.save(user);

        sendActivationEmail(user.getEmail(), token);

        return true;
    }

    private void sendActivationEmail(String email, String token) {
        String activationLink = "http://localhost:3000/activate?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Activate your account");
        message.setText("Click the link to activate your account: " + activationLink);
        mailSender.send(message);
    }

    public AuthenticationResponse activateAccount(String token) {
        User user = userRepository.findByActivationToken(token);
        if (user != null && !user.isActive()) {
            user.setActive(true);
            user.setActivationToken(null);
            user.setLastLogin(new Date());
            userRepository.save(user);
            var jwtToken = jwtService.generateToken(user);
            return AuthenticationResponse
                    .builder()
                    .token(jwtToken)
                    .build();
        }
        throw new IllegalArgumentException("Invalid activation token.");
    }

    public Collection<UserDto> getAll() {
        List<User> users = userRepository.findAll();
        return userMapper.usersToUserDTOs(users);
    }
}
