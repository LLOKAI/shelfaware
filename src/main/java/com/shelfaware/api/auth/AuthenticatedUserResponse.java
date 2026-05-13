package com.shelfaware.api.auth;

import com.shelfaware.domain.Role;
import java.util.Set;

public record AuthenticatedUserResponse(
    Long id,
    String displayName,
    String email,
    String username,
    Set<Role> roles
) {
}
