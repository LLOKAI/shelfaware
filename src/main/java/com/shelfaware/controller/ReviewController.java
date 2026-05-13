package com.shelfaware.controller;

import com.shelfaware.api.review.ReviewRequest;
import com.shelfaware.api.review.ReviewResponse;
import com.shelfaware.domain.UserAccount;
import com.shelfaware.service.ReviewService;
import com.shelfaware.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
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
    private final UserService userService;

    public ReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping("/books/{bookId}/reviews")
    public List<ReviewResponse> getPublicReviews(@PathVariable Long bookId) {
        return reviewService.getPublicReviews(bookId);
    }

    @PutMapping("/books/{bookId}/reviews/me")
    public ReviewResponse upsertReview(
        Principal principal,
        @PathVariable Long bookId,
        @Valid @RequestBody ReviewRequest request
    ) {
        UserAccount user = userService.getByUsername(principal.getName());
        return reviewService.upsertReview(user.getId(), bookId, request);
    }

    @GetMapping("/me/reviews")
    public List<ReviewResponse> getMyReviews(Principal principal) {
        UserAccount user = userService.getByUsername(principal.getName());
        return reviewService.getUserReviews(user.getId());
    }
}
