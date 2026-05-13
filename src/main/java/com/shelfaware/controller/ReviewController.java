package com.shelfaware.controller;

import com.shelfaware.api.review.ReviewRequest;
import com.shelfaware.api.review.ReviewResponse;
import com.shelfaware.security.CustomUserPrincipal;
import com.shelfaware.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/books/{bookId}/reviews")
    public List<ReviewResponse> getPublicReviews(@PathVariable Long bookId) {
        return reviewService.getPublicReviews(bookId);
    }

    @PutMapping("/books/{bookId}/reviews/me")
    public ReviewResponse upsertReview(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable Long bookId,
        @Valid @RequestBody ReviewRequest request
    ) {
        return reviewService.upsertReview(principal.getId(), bookId, request);
    }

    @GetMapping("/me/reviews")
    public List<ReviewResponse> getMyReviews(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return reviewService.getUserReviews(principal.getId());
    }
}
