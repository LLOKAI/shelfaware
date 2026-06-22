package com.shelfaware.api.journey;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProgressUpdateRequest(@Min(0) int currentPage, @NotNull LocalDate readOn) {
}
