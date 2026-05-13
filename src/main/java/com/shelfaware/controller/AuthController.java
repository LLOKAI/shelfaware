package com.shelfaware.controller;

import com.shelfaware.api.auth.AuthenticatedUserResponse;
import com.shelfaware.api.auth.RegisterRequest;
import com.shelfaware.security.CustomUserPrincipal;
import com.shelfaware.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public AuthenticatedUserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse me(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return userService.toResponse(userService.getUser(principal.getId()));
    }
}
