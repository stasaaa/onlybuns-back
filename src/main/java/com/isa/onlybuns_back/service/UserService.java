package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.dto.UpdateUserProfileDto;
import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.mapper.UserMapper;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.repository.LikeRepository;
import com.isa.onlybuns_back.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    public UserService(UserRepository userRepository,  LikeRepository likeRepository) {
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
    }

    public UserDto findById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        return user!=null ? UserMapper.toDto(user) : null;
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return user.getUsername();
    }

    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return findById(user.getId());
    }

    public Page<UserDto> findAll(Pageable pageable) {
        Page<User> pageUsers = userRepository.findAll(pageable);
        return pageUsers.map(UserMapper::toDto);
    }

    public Page<UserDto> findAllFiltered(String searchQuery, Integer minPosts, Integer maxPosts, Pageable pageable) {
        Page<User> pageUsers = userRepository.findFiltered(
                (searchQuery == null || searchQuery.isEmpty()) ? null : searchQuery,
                minPosts,
                maxPosts,
                pageable
        );

        return pageUsers.map(user -> {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setEmail(user.getEmail());
            dto.setUsername(user.getUsername());
            dto.setUserRole(user.getUserRole());
            dto.setActive(user.isActive());
            dto.setNumberOfPosts(user.getPosts() != null ? user.getPosts().size() : 0);
            dto.setNumberOfFollowing((int) userRepository.countFollowingByUserId(user.getId()));

            return dto;
        });
    }


    public List<UserDto> getAllUsersForGroupDialog(Long currentUserId) {
        if (currentUserId == null) {
            return List.of();
        }

        try {
            return userRepository.findAllExceptCurrent(currentUserId)
                    .stream()
                    .map(UserDto::new)  // koristi direktno konstruktor DTO klase
                    .toList();

        } catch (Exception e) {
            System.err.println("Error in getAllUsersForGroupDialog: " + e.getMessage());
            return List.of();
        }
    }

    public UserDto updateUser(UpdateUserProfileDto updateInfo) {
        User user = userRepository.findById(updateInfo.getId())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setFirstName(updateInfo.getFirstName())
                .setLastName(updateInfo.getLastName())
                .setUsername(updateInfo.getUsername());
        userRepository.save(user);
        return UserMapper.toDto(user);
    }


    public List<UserDto> getTopUsersLastWeek() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        Date oneWeekAgo = cal.getTime();

        List<Long> topUserIds = likeRepository.findTopUserIdsByLikesSince(oneWeekAgo)
                .stream()
                .limit(10)
                .toList();

        List<User> topUsers = new ArrayList<>();
        for (Long id : topUserIds) {
            userRepository.findById(id).ifPresent(topUsers::add);
        }

        return topUsers.stream()
                .map(UserMapper::toDto).toList();
    }
}