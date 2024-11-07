package com.isa.onlybuns_back.iservice;

import com.isa.onlybuns_back.dto.UserDto;

import javax.security.sasl.AuthenticationException;
import java.util.Collection;

public interface IAuthenticationService {
    public UserDto login(UserDto userDto);
    public boolean logout(UserDto userDto);
    public boolean register(UserDto userDto);
    public boolean activateAccount(String token);
    public Collection<UserDto> getAll();
}
