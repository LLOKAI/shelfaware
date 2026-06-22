package com.shelfaware.service;

import com.shelfaware.api.insights.ReadingInsightsResponse;
import com.shelfaware.domain.ReadingStatus;
import com.shelfaware.repository.ReviewRepository;
import com.shelfaware.repository.ShelfItemRepository;
import com.shelfaware.repository.ReadingSessionRepository;
import com.shelfaware.domain.ReadingSession;
import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InsightsService {

    private final ShelfItemRepository shelfItemRepository;
    private final ReviewRepository reviewRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final JourneyService journeyService;

    public InsightsService(
        ShelfItemRepository shelfItemRepository,
        ReviewRepository reviewRepository,
        ReadingSessionRepository readingSessionRepository,
        JourneyService journeyService
    ) {
        this.shelfItemRepository = shelfItemRepository;
        this.reviewRepository = reviewRepository;
        this.readingSessionRepository = readingSessionRepository;
        this.journeyService = journeyService;
    }

    @Transactional(readOnly = true)
    public ReadingInsightsResponse getInsights(Long userId) {
        long wantToRead = shelfItemRepository.countByUserIdAndStatus(userId, ReadingStatus.WANT_TO_READ);
        long reading = shelfItemRepository.countByUserIdAndStatus(userId, ReadingStatus.READING);
        long finished = shelfItemRepository.countByUserIdAndStatus(userId, ReadingStatus.FINISHED);
        long favorites = shelfItemRepository.countByUserIdAndFavoriteTrue(userId);

        List<Integer> ratings = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(review -> review.getRating())
            .toList();

        Map<String, Long> distribution = new LinkedHashMap<>();
        for (int rating = 1; rating <= 5; rating++) {
            int currentRating = rating;
            distribution.put(String.valueOf(rating), ratings.stream().filter(value -> value == currentRating).count());
        }

        double averageRating = ratings.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0);

        int year = LocalDate.now().getYear();
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        List<ReadingSession> sessions = readingSessionRepository
            .findByUserIdAndReadOnBetweenOrderByReadOnDescCreatedAtDesc(userId, from, to);
        long pagesRead = sessions.stream().mapToLong(ReadingSession::getPagesRead).sum();
        Map<String, Long> monthlyPages = new LinkedHashMap<>();
        Map<String, Long> monthlyBooks = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            monthlyPages.put(month.name().substring(0, 3), 0L);
            monthlyBooks.put(month.name().substring(0, 3), 0L);
        }
        sessions.forEach(session -> monthlyPages.compute(
            session.getReadOn().getMonth().name().substring(0, 3),
            (key, count) -> count + session.getPagesRead()
        ));
        shelfItemRepository.findByUserIdAndFinishedOnBetweenOrderByUpdatedAtDesc(userId, from, to).forEach(item -> monthlyBooks.compute(
            item.getFinishedOn().getMonth().name().substring(0, 3),
            (key, count) -> count + 1
        ));
        int currentStreak = journeyService.getJourney(userId, year).streak().currentDays();

        return new ReadingInsightsResponse(
            wantToRead + reading + finished,
            wantToRead,
            reading,
            finished,
            favorites,
            ratings.size(),
            averageRating,
            distribution,
            pagesRead,
            currentStreak,
            sessions.isEmpty() ? 0 : (double) pagesRead / sessions.size(),
            monthlyPages,
            monthlyBooks
        );
    }
}
