package com.shelfaware.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "shelfaware.cors")
public record CorsProperties(List<String> allowedOriginPatterns) {
}
