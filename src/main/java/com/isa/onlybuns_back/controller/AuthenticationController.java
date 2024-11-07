package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.iservice.IAuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.sasl.AuthenticationException;
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
    public ResponseEntity<Collection<UserDto>> GetAll() {
        var ret = authentificationService.getAll();
        return ResponseEntity.ok(ret);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody UserDto userDto){
        var ret = authentificationService.login(userDto);
        return ResponseEntity.ok(ret);
    }

    @PostMapping
    public ResponseEntity<Boolean> register(@Valid @RequestBody UserDto userDto) {
        var ret = authentificationService.register(userDto);
        return ResponseEntity.ok(ret)  ;
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateAccount(@RequestParam String token) {
        String ret;
        if(this.authentificationService.activateAccount(token)) {
            ret = "Account activated";
        } else {
            ret = "Account not activated";
        }
        return ResponseEntity.ok(ret);
    }
}
