package com.shelfaware.api.journey;

import jakarta.validation.constraints.Positive;

public record ReadingGoalRequest(@Positive Integer targetBooks, @Positive Integer targetPages) {
}
