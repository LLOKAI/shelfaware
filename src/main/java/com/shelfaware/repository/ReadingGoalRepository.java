package com.shelfaware.repository;

import com.shelfaware.domain.ReadingGoal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingGoalRepository extends JpaRepository<ReadingGoal, Long> {
    Optional<ReadingGoal> findByUserIdAndYear(Long userId, int year);
}
