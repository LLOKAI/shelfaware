package com.shelfaware.api.shelf;

import com.shelfaware.api.book.BookResponse;
import com.shelfaware.domain.ReadingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShelfItemResponse(
    Long id,
    BookResponse book,
    ReadingStatus status,
    LocalDate startedOn,
    LocalDate finishedOn,
    String privateNotes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
