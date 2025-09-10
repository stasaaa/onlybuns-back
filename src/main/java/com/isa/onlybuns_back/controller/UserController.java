package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.UpdateUserProfileDto;
import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @GetMapping
    public ResponseEntity<Page<UserDto>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Integer minPosts,
            @RequestParam(required = false) Integer maxPosts,
            @RequestParam(defaultValue = "email") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        List<String> allowedSortFields = List.of("email", "username", "firstName", "lastName");

        String sortField = allowedSortFields.contains(sort) ? sort : "email";

        Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sortField);

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<UserDto> usersPage = userService.findAllFiltered(
                (searchQuery != null && !searchQuery.isBlank()) ? searchQuery : null,
                minPosts,
                maxPosts,
                pageable
        );

        return ResponseEntity.ok(usersPage);
    }


    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllForGroupChat(@RequestParam Long currentUserId) {
        try {
            List<UserDto> users = userService.getAllUsersForGroupDialog(currentUserId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {

            System.err.println("Error getting users for group chat: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/simple")
    public ResponseEntity<List<UserDto>> getAllSimple() {
        try {
            List<UserDto> users = userService.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("Error getting all users: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    @PostMapping("")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody UpdateUserProfileDto updateProfileInfo) {
        return ResponseEntity.ok(userService.updateUser(updateProfileInfo));
    }

    @GetMapping("/ten-users-liking-most-last-week")
    public ResponseEntity<List<UserDto>> getMostActiveUsers() {
        return ResponseEntity.ok(userService.getTopUsersLastWeek());
    }
}

