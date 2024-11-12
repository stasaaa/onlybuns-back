package com.isa.onlybuns_back.mapper;

import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDto userToUserDTO(User user);
    User userDTOToUser(UserDto userDTO);
    Collection<UserDto> usersToUserDTOs(Collection<User> users);
    Collection<User> userDTOsToUsers(Collection<UserDto> userDTOs);
}
