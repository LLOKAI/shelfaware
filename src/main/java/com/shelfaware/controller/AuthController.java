package com.shelfaware.controller;

import com.shelfaware.api.auth.AuthResponse;
import com.shelfaware.api.auth.AuthenticatedUserResponse;
import com.shelfaware.api.auth.LoginRequest;
import com.shelfaware.api.auth.RegisterRequest;
import com.shelfaware.service.AuthService;
import com.shelfaware.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse me(Principal principal) {
        return userService.toResponse(userService.getByUsername(principal.getName()));
    }
}
