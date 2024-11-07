package com.isa.onlybuns_back.dto;

import com.isa.onlybuns_back.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDto {
    private long id;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password should be at least 8 characters long")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String passwordConfirm;

    @NotBlank(message = "Name is required")
    private String name;

    private AddressDto address;

    private UserRole role;
    private boolean isActive;
    private String activationToken;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public @Email(message = "Email should be valid") @NotBlank(message = "Email is required") String getEmail() {
        return email;
    }

    public void setEmail(@Email(message = "Email should be valid") @NotBlank(message = "Email is required") String email) {
        this.email = email;
    }

    public @NotBlank(message = "Username is required") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "Username is required") String username) {
        this.username = username;
    }

    public @NotBlank(message = "Password is required") @Size(min = 8, message = "Password should be at least 8 characters long") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is required") @Size(min = 8, message = "Password should be at least 8 characters long") String password) {
        this.password = password;
    }

    public @NotBlank(message = "Password confirmation is required") String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(@NotBlank(message = "Password confirmation is required") String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public @NotBlank(message = "Name is required") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Name is required") String name) {
        this.name = name;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getActivationToken() {
        return activationToken;
    }

    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }

    public AddressDto getAddress() {
        return address;
    }

    public void setAddress(AddressDto address) {
        this.address = address;
    }
}
