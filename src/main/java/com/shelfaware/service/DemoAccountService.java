package com.shelfaware.service;

import com.shelfaware.api.auth.RegisterRequest;
import com.shelfaware.domain.Book;
import com.shelfaware.domain.ReadingGoal;
import com.shelfaware.domain.ReadingSession;
import com.shelfaware.domain.ReadingStatus;
import com.shelfaware.domain.Review;
import com.shelfaware.domain.ShelfItem;
import com.shelfaware.domain.UserAccount;
import com.shelfaware.exception.ResourceNotFoundException;
import com.shelfaware.repository.BookRepository;
import com.shelfaware.repository.ReadingGoalRepository;
import com.shelfaware.repository.ReadingSessionRepository;
import com.shelfaware.repository.ReviewRepository;
import com.shelfaware.repository.ShelfItemRepository;
import com.shelfaware.repository.UserAccountRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoAccountService {
    private static final String DEMO_PREFIX = "demo_";

    private final UserService userService;
    private final UserAccountRepository userRepository;
    private final BookRepository bookRepository;
    private final ShelfItemRepository shelfRepository;
    private final ReviewRepository reviewRepository;
    private final ReadingGoalRepository goalRepository;
    private final ReadingSessionRepository sessionRepository;
    private final boolean enabled;

    public DemoAccountService(
        UserService userService,
        UserAccountRepository userRepository,
        BookRepository bookRepository,
        ShelfItemRepository shelfRepository,
        ReviewRepository reviewRepository,
        ReadingGoalRepository goalRepository,
        ReadingSessionRepository sessionRepository,
        @Value("${shelfaware.demo.enabled:true}") boolean enabled
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.shelfRepository = shelfRepository;
        this.reviewRepository = reviewRepository;
        this.goalRepository = goalRepository;
        this.sessionRepository = sessionRepository;
        this.enabled = enabled;
    }

    @Transactional
    public UserAccount createDemoAccount() {
        if (!enabled) {
            throw new ResourceNotFoundException("Demo mode is not enabled");
        }

        userRepository.deleteStaleDemoUsers(DEMO_PREFIX, LocalDateTime.now().minusDays(1));
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        UserAccount user = userService.registerUser(new RegisterRequest(
            "Alex Morgan", "demo+" + suffix + "@shelfaware.app", DEMO_PREFIX + suffix, UUID.randomUUID().toString()
        ));

        List<Book> books = bookRepository.findAll(Sort.by("id"));
        if (books.size() < 3) {
            throw new IllegalStateException("The demo library requires at least three books");
        }

        LocalDate today = LocalDate.now();
        ShelfItem reading = shelf(user, books.get(0), ReadingStatus.READING, 284, true, today.minusDays(18), null);
        ShelfItem finished = shelf(user, books.get(1), ReadingStatus.FINISHED, pageCount(books.get(1), 432), true, today.minusDays(70), today.minusDays(24));
        shelf(user, books.get(2), ReadingStatus.WANT_TO_READ, 0, false, null, null);

        seedSessions(reading, today, new int[] {42, 96, 151, 213, 250, 284}, new int[] {17, 13, 9, 6, 2, 1});
        seedSessions(finished, today, new int[] {68, 146, 231, 318, pageCount(books.get(1), 432)}, new int[] {66, 55, 43, 31, 24});
        review(user, books.get(0), 5, "Dense in the best way. The chapters on storage and distributed systems changed how I evaluate backend tradeoffs.");
        review(user, books.get(1), 4, "A sharp framework for thinking about boundaries. I disagreed with a few absolutes, but the core ideas stayed with me.");

        ReadingGoal goal = new ReadingGoal();
        goal.setUser(user);
        goal.setYear(today.getYear());
        goal.setTargetBooks(12);
        goal.setTargetPages(5_000);
        goalRepository.save(goal);
        return user;
    }

    private ShelfItem shelf(UserAccount user, Book book, ReadingStatus status, int currentPage, boolean favorite, LocalDate started, LocalDate finished) {
        ShelfItem item = new ShelfItem();
        item.setUser(user);
        item.setBook(book);
        item.setStatus(status);
        item.setCurrentPage(currentPage);
        item.setFavorite(favorite);
        item.setStartedOn(started);
        item.setFinishedOn(finished);
        item.setPrivateNotes(status == ReadingStatus.READING ? "Take notes on consistency models and practical system design examples." : null);
        return shelfRepository.save(item);
    }

    private void seedSessions(ShelfItem item, LocalDate today, int[] endPages, int[] daysAgo) {
        int startPage = 0;
        for (int index = 0; index < endPages.length; index++) {
            ReadingSession session = new ReadingSession();
            session.setShelfItem(item);
            session.setUser(item.getUser());
            session.setReadOn(today.minusDays(daysAgo[index]));
            session.setStartPage(startPage);
            session.setEndPage(endPages[index]);
            session.setPagesRead(endPages[index] - startPage);
            session.setCompletedBook(index == endPages.length - 1 && item.getStatus() == ReadingStatus.FINISHED);
            sessionRepository.save(session);
            startPage = endPages[index];
        }
    }

    private void review(UserAccount user, Book book, int rating, String body) {
        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(rating);
        review.setBody(body);
        review.setPublicReview(false);
        reviewRepository.save(review);
    }

    private int pageCount(Book book, int fallback) {
        return book.getPageCount() == null ? fallback : book.getPageCount();
    }
}
