package com.isa.onlybuns_back.dto;

import com.isa.onlybuns_back.model.Address;
import com.isa.onlybuns_back.model.User;
import com.isa.onlybuns_back.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Password confirmation is required")
    private String passwordConfirm;

    private Address address = new Address();

    private UserRole userRole;

    private boolean isActive;

    private String activationToken;

    private int numberOfPosts;

    private int numberOfFollowing = 0;



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

    public @NotBlank(message = "Username is required") String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NotBlank(message = "First name is required") String firstName) {
        this.firstName = firstName;
    }

    public @NotBlank(message = "Last name is required") String getLastName() {
        return lastName;
    }

    public void setLastName(@NotBlank(message = "Username is required") String lastName) {
        this.lastName = lastName;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public UserDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.numberOfPosts = user.getPosts().size();
    }

    public UserDto() {}

}
