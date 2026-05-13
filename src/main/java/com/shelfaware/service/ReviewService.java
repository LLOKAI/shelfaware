package com.shelfaware.service;

import com.shelfaware.api.review.ReviewRequest;
import com.shelfaware.api.review.ReviewResponse;
import com.shelfaware.domain.Book;
import com.shelfaware.domain.Review;
import com.shelfaware.domain.UserAccount;
import com.shelfaware.repository.ReviewRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookService bookService;
    private final UserService userService;

    public ReviewService(ReviewRepository reviewRepository, BookService bookService, UserService userService) {
        this.reviewRepository = reviewRepository;
        this.bookService = bookService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getPublicReviews(Long bookId) {
        return reviewRepository.findByBookIdAndPublicReviewTrueOrderByCreatedAtDesc(bookId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getUserReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ReviewResponse upsertReview(Long userId, Long bookId, ReviewRequest request) {
        UserAccount user = userService.getUser(userId);
        Book book = bookService.getBook(bookId);

        Review review = reviewRepository.findByUserAndBook(user, book).orElseGet(Review::new);
        review.setUser(user);
        review.setBook(book);
        review.setRating(request.rating());
        review.setBody(request.body().trim());
        review.setPublicReview(request.publicReview());

        return toResponse(reviewRepository.save(review));
    }

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
            review.getId(),
            review.getBook().getId(),
            review.getBook().getTitle(),
            review.getUser().getId(),
            review.getUser().getUsername(),
            review.getRating(),
            review.getBody(),
            review.isPublicReview(),
            review.getCreatedAt(),
            review.getUpdatedAt()
        );
    }
}
