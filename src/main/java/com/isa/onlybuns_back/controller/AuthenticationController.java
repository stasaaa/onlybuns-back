package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.iservice.IAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(path = "authentication")
public class AuthenticationController {

    private final IAuthenticationService authentificationService;

    @Autowired
    public AuthenticationController(IAuthenticationService authentificationService) {
        this.authentificationService = authentificationService;
    }

    @GetMapping
    public Collection<UserDto> GetAll() {
        return authentificationService.getAll();
    }

    @GetMapping("/activate")
    public String activateAccount(@RequestParam String token) {
        String ret;
        if(this.authentificationService.activateAccount(token)) {
            ret = "Account activated";
        } else {
            ret = "Account not activated";
        }
        return ret;
    }
}
