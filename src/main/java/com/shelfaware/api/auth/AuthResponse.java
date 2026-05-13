package com.shelfaware.api.auth;

import java.time.Instant;

public record AuthResponse(
    String tokenType,
    String accessToken,
    Instant expiresAt,
    AuthenticatedUserResponse user
) {
}
