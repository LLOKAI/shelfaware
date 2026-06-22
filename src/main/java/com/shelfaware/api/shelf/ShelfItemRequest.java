package com.shelfaware.api.shelf;

import com.shelfaware.domain.ReadingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ShelfItemRequest(
    @NotNull ReadingStatus status,
    LocalDate startedOn,
    LocalDate finishedOn,
    @Size(max = 2_000) String privateNotes,
    boolean favorite
) {
}
