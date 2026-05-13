package com.shelfaware.api.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BookCreateRequest(
    @NotBlank @Size(max = 180) String title,
    @NotBlank @Size(max = 220) String authors,
    @Size(max = 20) String isbn,
    @Size(max = 2_000) String description,
    @Size(max = 500) String coverImageUrl,
    @Size(max = 180) String publisher,
    @Size(max = 220) String categories,
    LocalDate publishedDate,
    @Min(1) @Max(10_000) Integer pageCount
) {
}
