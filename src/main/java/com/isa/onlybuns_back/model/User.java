package com.isa.onlybuns_back.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity(name = "User")
@Table(
        name = "Users",
        uniqueConstraints = {
                @UniqueConstraint(name = "user_email_unique", columnNames = "email"),
                @UniqueConstraint(name = "username_unique", columnNames = "username")
        }
)
public class User {
    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    @Column(
            name = "user_id",
            updatable = false
    )
    private long id;

    @Column(
            name = "email",
            unique = true,
            nullable = false
    )
    private String email;

    @Column(
            name = "username",
            unique = true,
            nullable = false
    )
    private String username;

    @Column(
            name = "user_password",
            nullable = false
    )
    private String password;

    @Column(
            name = "user_role",
            nullable = false
    )
    private UserRole userRole;

    @Column(
            name = "last_login",
            nullable = false
    )
    private Date lastLogin;

    @Column(
            name = "is_active",
            nullable = false
    )
    private boolean isActive;

    @Column(
            name = "activation_token",
            unique = true
    )
    private String activationToken;

    public User() {
    }

    public User(String email, String username, String password, UserRole userRole, Date lastLogin,
                boolean isActive, String activationToken) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.userRole = userRole;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
        this.activationToken = activationToken;
    }

    public User(long id, String email, String username, String password, UserRole userRole,
                Date lastLogin, boolean isActive, String activationToken) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.userRole = userRole;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
        this.activationToken = activationToken;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", userRole=" + userRole +
                ", lastLogin=" + lastLogin +
                ", isActive=" + isActive +
                ", activationToken='" + activationToken + '\'' +
                '}';
    }
}