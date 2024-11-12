package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto findById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setUserRole(user.getUserRole());
        userDto.setEmail(user.getEmail());
        userDto.setActive(user.isActive());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());

        userDto.getAddress().setCity(user.getAddress().getCity());
        userDto.getAddress().setCountry(user.getAddress().getCountry());
        userDto.getAddress().setStreet(user.getAddress().getStreet());
        userDto.getAddress().setPostalCode(user.getAddress().getPostalCode());
        userDto.getAddress().setNumber(user.getAddress().getNumber());

        return userDto;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::new) // Mapira User entitet u UserDto
                .collect(Collectors.toList());
    }

    public String findUsername(long id) {
        User user = userRepository.findById(id).orElse(null);
        assert user != null;
        return user.getUsername();
    }

    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return findById(user.getId());
    }
}