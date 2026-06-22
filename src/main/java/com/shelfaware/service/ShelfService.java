package com.shelfaware.service;

import com.shelfaware.api.shelf.ShelfItemRequest;
import com.shelfaware.api.shelf.ShelfItemResponse;
import com.shelfaware.domain.Book;
import com.shelfaware.domain.ReadingStatus;
import com.shelfaware.domain.ShelfItem;
import com.shelfaware.domain.UserAccount;
import com.shelfaware.repository.ShelfItemRepository;
import java.util.List;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShelfService {

    private final ShelfItemRepository shelfItemRepository;
    private final BookService bookService;
    private final UserService userService;

    public ShelfService(ShelfItemRepository shelfItemRepository, BookService bookService, UserService userService) {
        this.shelfItemRepository = shelfItemRepository;
        this.bookService = bookService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<ShelfItemResponse> getShelf(Long userId, ReadingStatus status) {
        List<ShelfItem> items = status == null
            ? shelfItemRepository.findByUserIdOrderByUpdatedAtDesc(userId)
            : shelfItemRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(userId, status);

        return items.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ShelfItemResponse upsertShelfItem(Long userId, Long bookId, ShelfItemRequest request) {
        UserAccount user = userService.getUser(userId);
        Book book = bookService.getBook(bookId);

        ShelfItem item = shelfItemRepository.findByUserAndBook(user, book).orElseGet(ShelfItem::new);
        item.setUser(user);
        item.setBook(book);
        item.setStatus(request.status());
        if (request.status() == ReadingStatus.WANT_TO_READ) {
            item.setStartedOn(null);
            item.setFinishedOn(null);
        } else if (request.status() == ReadingStatus.READING) {
            item.setStartedOn(firstNonNull(request.startedOn(), item.getStartedOn(), LocalDate.now()));
            item.setFinishedOn(null);
        } else {
            item.setStartedOn(firstNonNull(request.startedOn(), item.getStartedOn(), LocalDate.now()));
            item.setFinishedOn(firstNonNull(request.finishedOn(), item.getFinishedOn(), LocalDate.now()));
        }
        item.setPrivateNotes(request.privateNotes());
        item.setFavorite(request.favorite());

        return toResponse(shelfItemRepository.save(item));
    }

    public ShelfItemResponse toResponse(ShelfItem item) {
        return new ShelfItemResponse(
            item.getId(),
            bookService.toResponse(item.getBook()),
            item.getStatus(),
            item.getStartedOn(),
            item.getFinishedOn(),
            item.getCurrentPage(),
            item.isFavorite(),
            item.getPrivateNotes(),
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ShelfItem getShelfItem(Long userId, Long bookId) {
        UserAccount user = userService.getUser(userId);
        Book book = bookService.getBook(bookId);
        return shelfItemRepository.findByUserAndBook(user, book)
            .orElseThrow(() -> new com.shelfaware.exception.ResourceNotFoundException("Book is not on your shelf"));
    }

    private LocalDate firstNonNull(LocalDate first, LocalDate second, LocalDate fallback) {
        return first != null ? first : second != null ? second : fallback;
    }
}
