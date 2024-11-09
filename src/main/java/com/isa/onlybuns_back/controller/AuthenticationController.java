package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.AuthenticationRequest;
import com.isa.onlybuns_back.dto.AuthenticationResponse;
import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping(path = "authentication")
public class AuthenticationController {

    private final AuthenticationService authentificationService;

    @Autowired
    public AuthenticationController(AuthenticationService authentificationService) {
        this.authentificationService = authentificationService;
    }

    @PostMapping("login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        var ret = authentificationService.login(authenticationRequest);
        return ResponseEntity.ok(ret);
    }

    @PostMapping("register")
    public ResponseEntity<Boolean> register(@Valid @RequestBody UserDto userDto) {
        var ret = authentificationService.register(userDto);
        return ResponseEntity.ok(ret)  ;
    }

    @GetMapping("activate")
    public ResponseEntity<AuthenticationResponse> activateAccount(@RequestParam String token) {
        var ret = this.authentificationService.activateAccount(token);
        return ResponseEntity.ok(ret);
    }

    @GetMapping("userDetails")
    public ResponseEntity<UserDto> getUserDetails(@RequestParam String email) {
        var ret = this.authentificationService.userDetails(email);
        return ResponseEntity.ok(ret);
    }
}
