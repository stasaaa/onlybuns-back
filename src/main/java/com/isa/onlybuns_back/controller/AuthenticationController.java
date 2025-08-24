package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.dto.AuthenticationRequest;
import com.isa.onlybuns_back.dto.AuthenticationResponse;
import com.isa.onlybuns_back.dto.UpdatePasswordDto;
import com.isa.onlybuns_back.dto.UserDto;
import com.isa.onlybuns_back.security.LoginAttemptService;
import com.isa.onlybuns_back.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collection;

@RestController
@RequestMapping(path = "authentication")
public class AuthenticationController {

    private final AuthenticationService authentificationService;
    private final LoginAttemptService loginAttemptService;
    private final HandlerMapping resourceHandlerMapping;

    @Autowired
    public AuthenticationController(AuthenticationService authentificationService, LoginAttemptService loginAttemptService, HandlerMapping resourceHandlerMapping) {
        this.authentificationService = authentificationService;
        this.loginAttemptService = loginAttemptService;
        this.resourceHandlerMapping = resourceHandlerMapping;
    }

    @PostMapping("login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest authenticationRequest,
            HttpServletRequest request) {

        String clientIp = getClientIp(request); // Get client IP

        // Check if the IP is blocked due to too many failed attempts
        if (loginAttemptService.isBlocked(clientIp)) {
            return ResponseEntity.status(429).body(new AuthenticationResponse("Too many login attempts. Please try again later."));
        }

        try {
            var authenticationResponse = authentificationService.login(authenticationRequest);
            loginAttemptService.loginSucceeded(clientIp);  // Reset the failed attempts if login is successful
            return ResponseEntity.ok(authenticationResponse);
        } catch (Exception e) {
            loginAttemptService.loginFailed(clientIp);  // Increment failed attempt count on failure
            return ResponseEntity.status(401).body(new AuthenticationResponse("Invalid credentials"));
        }
    }

    @PostMapping("register")
    public ResponseEntity<Boolean> register(@Valid @RequestBody UserDto userDto) {
        var ret = authentificationService.register(userDto);
        return ResponseEntity.ok(ret) ;
    }

    @GetMapping("activate")
    public ResponseEntity<AuthenticationResponse> activateAccount(@RequestParam String token) {
        var ret = this.authentificationService.activateAccount(token);
        return ResponseEntity.ok(ret);
    }

    @PutMapping("update-password")
    public ResponseEntity<UserDto> updatePassword(@Valid @RequestBody UpdatePasswordDto updatePasswordInfo) {
        return ResponseEntity.ok(authentificationService.updatePassword(updatePasswordInfo));
    }

    @GetMapping("userDetails")
    public ResponseEntity<UserDto> getUserDetails(@RequestParam String email) {
        var ret = this.authentificationService.userDetails(email);
        return ResponseEntity.ok(ret);
    }

    @GetMapping("test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Test je prosao");
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIp;

        // Try to get the IP address from the X-Forwarded-For header (used by proxies and load balancers)
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            // If multiple IPs are in the X-Forwarded-For header (e.g., because of proxies), get the first one
            clientIp = xForwardedForHeader.split(",")[0];
        } else {
            // If no X-Forwarded-For header, fall back to the remote address
            clientIp = request.getRemoteAddr();
        }

        return clientIp;
    }
}
