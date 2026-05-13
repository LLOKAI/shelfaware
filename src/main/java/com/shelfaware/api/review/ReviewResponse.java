package com.shelfaware.api.review;

import java.time.LocalDateTime;

public record ReviewResponse(
    Long id,
    Long bookId,
    String bookTitle,
    Long userId,
    String username,
    int rating,
    String body,
    boolean publicReview,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
