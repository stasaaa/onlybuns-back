package com.isa.onlybuns_back.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN(
            Set.of(
                    Permission.ADMIN
            )
    ),
    REGISTERED(
            Set.of(
                    Permission.REGISTERED
            )
    )
    ;

    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getGrantedAuthority() {
        var authorities = new java.util.ArrayList<>(getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.name()))
                .toList());
        authorities.add(new SimpleGrantedAuthority(this.name()));
        return authorities;
    }
}
