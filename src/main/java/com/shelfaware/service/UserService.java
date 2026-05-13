package com.shelfaware.service;

import com.shelfaware.api.auth.AuthenticatedUserResponse;
import com.shelfaware.api.auth.RegisterRequest;
import com.shelfaware.domain.Role;
import com.shelfaware.domain.UserAccount;
import com.shelfaware.exception.ConflictException;
import com.shelfaware.exception.ResourceNotFoundException;
import com.shelfaware.repository.UserAccountRepository;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthenticatedUserResponse register(RegisterRequest request) {
        return toResponse(registerUser(request));
    }

    @Transactional
    public UserAccount registerUser(RegisterRequest request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email is already registered");
        }

        if (userAccountRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ConflictException("Username is already taken");
        }

        UserAccount user = new UserAccount();
        user.setDisplayName(request.displayName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setUsername(request.username().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.getRoles().add(Role.USER);

        return userAccountRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserAccount getUser(Long id) {
        return userAccountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserAccount getByUsername(String username) {
        return userAccountRepository.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public AuthenticatedUserResponse toResponse(UserAccount user) {
        return new AuthenticatedUserResponse(
            user.getId(),
            user.getDisplayName(),
            user.getEmail(),
            user.getUsername(),
            Set.copyOf(user.getRoles())
        );
    }
}
