package com.shelfaware.api.insights;

import java.util.Map;

public record ReadingInsightsResponse(
    long totalShelfItems,
    long wantToReadCount,
    long readingCount,
    long finishedCount,
    long favoriteCount,
    long reviewCount,
    double averageRating,
    Map<String, Long> ratingsDistribution
) {
}
