package com.shelfaware.repository;

import com.shelfaware.domain.Book;
import com.shelfaware.domain.Review;
import com.shelfaware.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookIdAndPublicReviewTrueOrderByCreatedAtDesc(Long bookId);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Review> findByUserAndBook(UserAccount user, Book book);
}
