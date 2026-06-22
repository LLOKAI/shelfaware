package com.shelfaware.service;

import com.shelfaware.api.auth.AuthResponse;
import com.shelfaware.api.auth.LoginRequest;
import com.shelfaware.api.auth.RegisterRequest;
import com.shelfaware.domain.UserAccount;
import com.shelfaware.exception.UnauthorizedException;
import com.shelfaware.repository.UserAccountRepository;
import com.shelfaware.security.JwtService;
import com.shelfaware.security.JwtService.IssuedToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserService userService;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DemoAccountService demoAccountService;

    public AuthService(
        UserService userService,
        UserAccountRepository userAccountRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        DemoAccountService demoAccountService
    ) {
        this.userService = userService;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.demoAccountService = demoAccountService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        UserAccount user = userService.registerUser(request);
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findByUsernameIgnoreCase(request.usernameOrEmail())
            .or(() -> userAccountRepository.findByEmailIgnoreCase(request.usernameOrEmail()))
            .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        return toAuthResponse(user);
    }

    @Transactional
    public AuthResponse createDemo() {
        return toAuthResponse(demoAccountService.createDemoAccount());
    }

    private AuthResponse toAuthResponse(UserAccount user) {
        IssuedToken token = jwtService.issueToken(user);
        return new AuthResponse(
            "Bearer",
            token.token(),
            token.expiresAt(),
            userService.toResponse(user)
        );
    }
}
