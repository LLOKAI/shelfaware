package com.shelfaware.service;

import com.shelfaware.api.insights.ReadingInsightsResponse;
import com.shelfaware.domain.ReadingStatus;
import com.shelfaware.repository.ReviewRepository;
import com.shelfaware.repository.ShelfItemRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InsightsService {

    private final ShelfItemRepository shelfItemRepository;
    private final ReviewRepository reviewRepository;

    public InsightsService(ShelfItemRepository shelfItemRepository, ReviewRepository reviewRepository) {
        this.shelfItemRepository = shelfItemRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public ReadingInsightsResponse getInsights(Long userId) {
        long wantToRead = shelfItemRepository.countByUserIdAndStatus(userId, ReadingStatus.WANT_TO_READ);
        long reading = shelfItemRepository.countByUserIdAndStatus(userId, ReadingStatus.READING);
        long finished = shelfItemRepository.countByUserIdAndStatus(userId, ReadingStatus.FINISHED);
        long favorites = shelfItemRepository.countByUserIdAndStatus(userId, ReadingStatus.FAVORITE);

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

        return new ReadingInsightsResponse(
            wantToRead + reading + finished + favorites,
            wantToRead,
            reading,
            finished,
            favorites,
            ratings.size(),
            averageRating,
            distribution
        );
    }
}
