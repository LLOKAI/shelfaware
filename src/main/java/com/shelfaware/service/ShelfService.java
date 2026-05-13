package com.shelfaware.service;

import com.shelfaware.api.shelf.ShelfItemRequest;
import com.shelfaware.api.shelf.ShelfItemResponse;
import com.shelfaware.domain.Book;
import com.shelfaware.domain.ReadingStatus;
import com.shelfaware.domain.ShelfItem;
import com.shelfaware.domain.UserAccount;
import com.shelfaware.repository.ShelfItemRepository;
import java.util.List;
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
        item.setStartedOn(request.startedOn());
        item.setFinishedOn(request.finishedOn());
        item.setPrivateNotes(request.privateNotes());

        return toResponse(shelfItemRepository.save(item));
    }

    public ShelfItemResponse toResponse(ShelfItem item) {
        return new ShelfItemResponse(
            item.getId(),
            bookService.toResponse(item.getBook()),
            item.getStatus(),
            item.getStartedOn(),
            item.getFinishedOn(),
            item.getPrivateNotes(),
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }
}
