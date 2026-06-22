package com.shelfaware.api.journey;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReadingSessionResponse(
    Long id,
    Long bookId,
    String bookTitle,
    String coverImageUrl,
    LocalDate readOn,
    int startPage,
    int endPage,
    int pagesRead,
    boolean completedBook,
    LocalDateTime createdAt
) {
}
