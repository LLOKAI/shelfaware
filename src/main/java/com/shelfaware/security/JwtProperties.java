package com.shelfaware.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shelfaware.security")
public record JwtProperties(
    String jwtSecret,
    String jwtIssuer,
    long jwtExpirationMinutes
) {
}
