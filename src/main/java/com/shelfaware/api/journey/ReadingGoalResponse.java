package com.shelfaware.api.journey;

public record ReadingGoalResponse(
    int year,
    Integer targetBooks,
    Integer targetPages,
    long completedBooks,
    long pagesRead,
    double booksProgress,
    double pagesProgress,
    long projectedBooks,
    long projectedPages
) {
}
