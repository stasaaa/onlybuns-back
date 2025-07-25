package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> findAll() { return userService.findAll(); }

    @GetMapping("{id}")
    public UserDto findById(@PathVariable long id) {
        return userService.findById(id);
    }

    @GetMapping("findUsername/{id}")
    public String findUsername(@PathVariable long id) {
        return userService.findUsername(id);
    }

    @GetMapping("find/{username}")
    public UserDto findByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }
}

