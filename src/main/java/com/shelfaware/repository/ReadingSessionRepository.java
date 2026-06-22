package com.shelfaware.repository;

import com.shelfaware.domain.ReadingSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    List<ReadingSession> findByUserIdAndReadOnBetweenOrderByReadOnDescCreatedAtDesc(Long userId, LocalDate from, LocalDate to);
    List<ReadingSession> findTop10ByUserIdOrderByReadOnDescCreatedAtDesc(Long userId);
    Optional<ReadingSession> findFirstByShelfItemIdOrderByCreatedAtDesc(Long shelfItemId);
}
