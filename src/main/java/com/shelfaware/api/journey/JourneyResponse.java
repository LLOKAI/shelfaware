package com.shelfaware.api.journey;

import com.shelfaware.api.shelf.ShelfItemResponse;
import java.util.List;

public record JourneyResponse(
    int year,
    ReadingGoalResponse goal,
    StreakResponse streak,
    List<ShelfItemResponse> activeBooks,
    List<ReadingSessionResponse> recentSessions,
    long pagesRead,
    long completedBooks
) {
}
