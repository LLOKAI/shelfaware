package com.shelfaware.api.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
    @Min(1) @Max(5) int rating,
    @NotBlank @Size(max = 4_000) String body,
    boolean publicReview
) {
}
