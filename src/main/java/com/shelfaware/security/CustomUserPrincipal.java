package com.shelfaware.security;

import com.shelfaware.domain.Role;
import com.shelfaware.domain.UserAccount;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String username;
    private final String passwordHash;
    private final Set<Role> roles;

    public CustomUserPrincipal(UserAccount user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.roles = Set.copyOf(user.getRoles());
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
            .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
