package com.shelfaware.api.journey;

import com.shelfaware.api.shelf.ShelfItemResponse;

public record ProgressUpdateResponse(ShelfItemResponse shelfItem, ReadingSessionResponse session, String milestone) {
}
