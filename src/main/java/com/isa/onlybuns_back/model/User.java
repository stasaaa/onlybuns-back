package com.isa.onlybuns_back.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Setter
@Entity(name = "User")
@Table(
        name = "Users",
        uniqueConstraints = {
                @UniqueConstraint(name = "user_email_unique", columnNames = "email"),
                @UniqueConstraint(name = "username_unique", columnNames = "username")
        }
)
public class User implements UserDetails {
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

    // Assign the encoded password
    @Column(
            name = "user_password",
            nullable = false
    )
    private String password;

    @Column(
            name = "first_name",
            nullable = false
    )
    private String firstName;

    @Column(
            name = "last_name",
            nullable = false
    )
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "user_role",
            nullable = false
    )
    private UserRole userRole;

    @Column(
            name = "last_login"
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

    @Embedded
    private Address address = new Address();

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public User() {
    }

    public User(String email, String username, String password, String firstName, String lastName,
                UserRole userRole, Date lastLogin, boolean isActive, String activationToken, Address address) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userRole = userRole;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
        this.activationToken = activationToken;
        this.address = address;
    }

    public User(Long id, String email, String username, String password, String firstName,
                String lastName, UserRole userRole, Date lastLogin, boolean isActive,
                String activationToken, Address address) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userRole = userRole;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
        this.activationToken = activationToken;
        this.address = address;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<GrantedAuthority>(this.userRole.getGrantedAuthority());
    }
}