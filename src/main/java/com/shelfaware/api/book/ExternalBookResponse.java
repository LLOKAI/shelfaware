package com.shelfaware.api.book;

import java.time.LocalDate;

public record ExternalBookResponse(
    String externalSource,
    String externalId,
    String title,
    String authors,
    String isbn,
    String description,
    String coverImageUrl,
    String publisher,
    String categories,
    LocalDate publishedDate,
    Integer pageCount
) {
}
