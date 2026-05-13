package com.shelfaware.controller;

import com.shelfaware.api.book.BookCreateRequest;
import com.shelfaware.api.book.BookResponse;
import com.shelfaware.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Page<BookResponse> search(
        @RequestParam(required = false) String q,
        @PageableDefault(size = 20, sort = "title") Pageable pageable
    ) {
        return bookService.search(q, pageable);
    }

    @GetMapping("/{bookId}")
    public BookResponse getBook(@PathVariable Long bookId) {
        return bookService.getBookResponse(bookId);
    }

    @PostMapping
    public BookResponse createBook(@Valid @RequestBody BookCreateRequest request) {
        return bookService.create(request);
    }
}
