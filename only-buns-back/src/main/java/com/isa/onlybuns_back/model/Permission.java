package com.isa.onlybuns_back.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {

    REGISTERED("registered"),
    ADMIN("admin");
    ;

    private final String permission;
}
