package com.shelfaware.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shelfaware.external.open-library")
public record OpenLibraryProperties(String baseUrl) {
}
