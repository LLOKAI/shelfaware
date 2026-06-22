package com.shelfaware.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.shelfaware.domain.ReadingSession;
import com.shelfaware.repository.ReadingGoalRepository;
import com.shelfaware.repository.ReadingSessionRepository;
import com.shelfaware.repository.ShelfItemRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JourneyServiceTests {
    @Mock ReadingSessionRepository sessionRepository;
    @Mock ReadingGoalRepository goalRepository;
    @Mock ShelfItemRepository shelfItemRepository;
    @Mock ShelfService shelfService;
    @Mock UserService userService;

    @Test
    void streakAllowsYesterdayAndStopsAtDateGaps() {
        LocalDate today = LocalDate.now();
        List<ReadingSession> sessions = List.of(
            session(today.minusDays(1)), session(today.minusDays(2)),
            session(today.minusDays(4)), session(today.minusDays(5)), session(today.minusDays(6))
        );
        int year = today.getYear();
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        when(sessionRepository.findByUserIdAndReadOnBetweenOrderByReadOnDescCreatedAtDesc(1L, from, to)).thenReturn(sessions);
        when(sessionRepository.findTop10ByUserIdOrderByReadOnDescCreatedAtDesc(1L)).thenReturn(List.of());
        when(goalRepository.findByUserIdAndYear(1L, year)).thenReturn(Optional.empty());
        when(shelfItemRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(1L, com.shelfaware.domain.ReadingStatus.READING)).thenReturn(List.of());

        JourneyService service = new JourneyService(sessionRepository, goalRepository, shelfItemRepository, shelfService, userService);
        var journey = service.getJourney(1L, year);

        assertThat(journey.streak().currentDays()).isEqualTo(2);
        assertThat(journey.streak().longestDays()).isEqualTo(3);
    }

    private ReadingSession session(LocalDate date) {
        ReadingSession session = new ReadingSession();
        session.setReadOn(date);
        session.setPagesRead(10);
        return session;
    }
}
