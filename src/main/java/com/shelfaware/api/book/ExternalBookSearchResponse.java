package com.shelfaware.api.book;

import java.util.List;

public record ExternalBookSearchResponse(
    String query,
    long totalResults,
    List<ExternalBookResponse> books
) {
}
