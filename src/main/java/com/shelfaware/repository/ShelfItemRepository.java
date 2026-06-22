package com.shelfaware.repository;

import com.shelfaware.domain.Book;
import com.shelfaware.domain.ReadingStatus;
import com.shelfaware.domain.ShelfItem;
import com.shelfaware.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShelfItemRepository extends JpaRepository<ShelfItem, Long> {

    List<ShelfItem> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<ShelfItem> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, ReadingStatus status);

    Optional<ShelfItem> findByUserAndBook(UserAccount user, Book book);

    long countByUserIdAndStatus(Long userId, ReadingStatus status);

    long countByUserIdAndFinishedOnBetween(Long userId, java.time.LocalDate from, java.time.LocalDate to);

    long countByUserIdAndFavoriteTrue(Long userId);

    List<ShelfItem> findByUserIdAndFinishedOnBetweenOrderByUpdatedAtDesc(Long userId, java.time.LocalDate from, java.time.LocalDate to);
}
