package com.shelfaware.service;

import com.shelfaware.api.journey.JourneyResponse;
import com.shelfaware.api.journey.ProgressUpdateRequest;
import com.shelfaware.api.journey.ProgressUpdateResponse;
import com.shelfaware.api.journey.ReadingGoalRequest;
import com.shelfaware.api.journey.ReadingGoalResponse;
import com.shelfaware.api.journey.ReadingSessionResponse;
import com.shelfaware.api.journey.StreakResponse;
import com.shelfaware.domain.ReadingGoal;
import com.shelfaware.domain.ReadingSession;
import com.shelfaware.domain.ReadingStatus;
import com.shelfaware.domain.ShelfItem;
import com.shelfaware.domain.UserAccount;
import com.shelfaware.exception.BadRequestException;
import com.shelfaware.exception.ConflictException;
import com.shelfaware.exception.ResourceNotFoundException;
import com.shelfaware.repository.ReadingGoalRepository;
import com.shelfaware.repository.ReadingSessionRepository;
import com.shelfaware.repository.ShelfItemRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JourneyService {
    private final ReadingSessionRepository sessionRepository;
    private final ReadingGoalRepository goalRepository;
    private final ShelfItemRepository shelfItemRepository;
    private final ShelfService shelfService;
    private final UserService userService;

    public JourneyService(
        ReadingSessionRepository sessionRepository,
        ReadingGoalRepository goalRepository,
        ShelfItemRepository shelfItemRepository,
        ShelfService shelfService,
        UserService userService
    ) {
        this.sessionRepository = sessionRepository;
        this.goalRepository = goalRepository;
        this.shelfItemRepository = shelfItemRepository;
        this.shelfService = shelfService;
        this.userService = userService;
    }

    @Transactional
    public ProgressUpdateResponse updateProgress(Long userId, Long bookId, ProgressUpdateRequest request) {
        ShelfItem item = shelfService.getShelfItem(userId, bookId);
        int previousPage = item.getCurrentPage();
        int nextPage = request.currentPage();
        Integer pageCount = item.getBook().getPageCount();

        if (nextPage <= previousPage) {
            throw new ConflictException("New progress must be greater than the current page");
        }
        if (pageCount != null && nextPage > pageCount) {
            throw new BadRequestException("Current page cannot exceed the book's page count");
        }

        if (item.getStatus() == ReadingStatus.WANT_TO_READ) {
            item.setStatus(ReadingStatus.READING);
            item.setStartedOn(request.readOn());
        } else if (item.getStartedOn() == null) {
            item.setStartedOn(request.readOn());
        }

        boolean completed = pageCount != null && nextPage == pageCount;
        if (completed) {
            item.setStatus(ReadingStatus.FINISHED);
            item.setFinishedOn(request.readOn());
        }
        item.setCurrentPage(nextPage);
        shelfItemRepository.save(item);

        ReadingSession session = new ReadingSession();
        session.setShelfItem(item);
        session.setUser(item.getUser());
        session.setReadOn(request.readOn());
        session.setStartPage(previousPage);
        session.setEndPage(nextPage);
        session.setPagesRead(nextPage - previousPage);
        session.setCompletedBook(completed);
        sessionRepository.save(session);

        String milestone = completed ? "Book finished" : nextPage >= 100 && previousPage < 100 ? "100 pages reached" : null;
        return new ProgressUpdateResponse(shelfService.toResponse(item), toSessionResponse(session), milestone);
    }

    @Transactional
    public void undoSession(Long userId, Long sessionId) {
        ReadingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Reading session not found"));
        if (!session.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Reading session not found");
        }
        ReadingSession latest = sessionRepository.findFirstByShelfItemIdOrderByCreatedAtDesc(session.getShelfItem().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Reading session not found"));
        if (!latest.getId().equals(sessionId)) {
            throw new ConflictException("Only the latest session for a book can be undone");
        }

        ShelfItem item = session.getShelfItem();
        item.setCurrentPage(session.getStartPage());
        if (session.isCompletedBook()) {
            item.setStatus(ReadingStatus.READING);
            item.setFinishedOn(null);
        }
        shelfItemRepository.save(item);
        sessionRepository.delete(session);
    }

    @Transactional
    public ReadingGoalResponse saveGoal(Long userId, int year, ReadingGoalRequest request) {
        validateYear(year);
        if (request.targetBooks() == null && request.targetPages() == null) {
            throw new BadRequestException("At least one reading goal target is required");
        }
        UserAccount user = userService.getUser(userId);
        ReadingGoal goal = goalRepository.findByUserIdAndYear(userId, year).orElseGet(ReadingGoal::new);
        goal.setUser(user);
        goal.setYear(year);
        goal.setTargetBooks(request.targetBooks());
        goal.setTargetPages(request.targetPages());
        goalRepository.save(goal);
        return goalResponse(userId, year, goal);
    }

    @Transactional(readOnly = true)
    public ReadingGoalResponse getGoal(Long userId, int year) {
        validateYear(year);
        return goalResponse(userId, year, goalRepository.findByUserIdAndYear(userId, year).orElse(null));
    }

    @Transactional(readOnly = true)
    public JourneyResponse getJourney(Long userId, int year) {
        validateYear(year);
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        List<ReadingSession> yearly = sessionRepository.findByUserIdAndReadOnBetweenOrderByReadOnDescCreatedAtDesc(userId, from, to);
        long pagesRead = yearly.stream().mapToLong(ReadingSession::getPagesRead).sum();
        long completedBooks = shelfItemRepository.countByUserIdAndFinishedOnBetween(userId, from, to);
        List<com.shelfaware.api.shelf.ShelfItemResponse> active = shelfItemRepository
            .findByUserIdAndStatusOrderByUpdatedAtDesc(userId, ReadingStatus.READING)
            .stream().map(shelfService::toResponse).toList();
        List<ReadingSessionResponse> recent = sessionRepository.findTop10ByUserIdOrderByReadOnDescCreatedAtDesc(userId)
            .stream().map(this::toSessionResponse).toList();
        ReadingGoal goal = goalRepository.findByUserIdAndYear(userId, year).orElse(null);
        return new JourneyResponse(year, goalResponse(userId, year, goal), streak(yearly, year), active, recent, pagesRead, completedBooks);
    }

    private ReadingGoalResponse goalResponse(Long userId, int year, ReadingGoal goal) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        List<ReadingSession> sessions = sessionRepository.findByUserIdAndReadOnBetweenOrderByReadOnDescCreatedAtDesc(userId, from, to);
        long pages = sessions.stream().mapToLong(ReadingSession::getPagesRead).sum();
        long books = shelfItemRepository.countByUserIdAndFinishedOnBetween(userId, from, to);
        Integer targetBooks = goal == null ? null : goal.getTargetBooks();
        Integer targetPages = goal == null ? null : goal.getTargetPages();
        int totalDays = from.lengthOfYear();
        LocalDate today = LocalDate.now();
        int elapsed = year < today.getYear() ? totalDays : year > today.getYear() ? 0 : today.getDayOfYear();
        long projectedBooks = elapsed == 0 ? 0 : Math.round((double) books / elapsed * totalDays);
        long projectedPages = elapsed == 0 ? 0 : Math.round((double) pages / elapsed * totalDays);
        return new ReadingGoalResponse(
            year, targetBooks, targetPages, books, pages,
            targetBooks == null ? 0 : Math.min(100, books * 100.0 / targetBooks),
            targetPages == null ? 0 : Math.min(100, pages * 100.0 / targetPages),
            projectedBooks, projectedPages
        );
    }

    private StreakResponse streak(List<ReadingSession> sessions, int year) {
        Set<LocalDate> dates = new TreeSet<>(Comparator.reverseOrder());
        sessions.stream().map(ReadingSession::getReadOn).forEach(dates::add);
        int longest = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate date : dates.stream().sorted().toList()) {
            run = previous != null && ChronoUnit.DAYS.between(previous, date) == 1 ? run + 1 : 1;
            longest = Math.max(longest, run);
            previous = date;
        }
        LocalDate today = LocalDate.now();
        LocalDate anchor = dates.contains(today) ? today : dates.contains(today.minusDays(1)) ? today.minusDays(1) : null;
        int current = 0;
        while (anchor != null && dates.contains(anchor.minusDays(current))) {
            current++;
        }
        return new StreakResponse(current, longest);
    }

    private ReadingSessionResponse toSessionResponse(ReadingSession session) {
        return new ReadingSessionResponse(
            session.getId(), session.getShelfItem().getBook().getId(), session.getShelfItem().getBook().getTitle(),
            session.getShelfItem().getBook().getCoverImageUrl(), session.getReadOn(), session.getStartPage(),
            session.getEndPage(), session.getPagesRead(), session.isCompletedBook(), session.getCreatedAt()
        );
    }

    private void validateYear(int year) {
        if (year < 2000 || year > 2100) {
            throw new BadRequestException("Goal year must be between 2000 and 2100");
        }
    }
}
