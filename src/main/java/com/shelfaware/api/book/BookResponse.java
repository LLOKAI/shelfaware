package com.shelfaware.api.book;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookResponse(
    Long id,
    String title,
    String authors,
    String isbn,
    String description,
    String coverImageUrl,
    String publisher,
    String categories,
    LocalDate publishedDate,
    Integer pageCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
