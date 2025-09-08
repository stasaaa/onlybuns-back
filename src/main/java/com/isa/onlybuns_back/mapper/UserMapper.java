package com.isa.onlybuns_back.mapper;

import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.model.Address;
import com.isa.onlybuns_back.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userRole(user.getUserRole())
                .address(user.getAddress() != null ? user.getAddress() : new Address())
                .isActive(user.isActive())
                .build();
    }

    public static User toEntity(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .userRole(userDto.getUserRole())
                .address(userDto.getAddress())
                .isActive(userDto.isActive())
                .build();
    }
}
