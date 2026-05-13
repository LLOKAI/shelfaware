package com.shelfaware.service;

import com.shelfaware.api.book.BookResponse;
import com.shelfaware.api.book.ExternalBookImportRequest;
import com.shelfaware.api.book.ExternalBookSearchResponse;
import com.shelfaware.domain.Book;
import com.shelfaware.external.OpenLibraryClient;
import com.shelfaware.external.OpenLibraryClient.SearchResult;
import com.shelfaware.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ExternalBookService {

    private final OpenLibraryClient openLibraryClient;
    private final BookRepository bookRepository;
    private final BookService bookService;

    public ExternalBookService(
        OpenLibraryClient openLibraryClient,
        BookRepository bookRepository,
        BookService bookService
    ) {
        this.openLibraryClient = openLibraryClient;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
    }

    public ExternalBookSearchResponse search(String query, int limit) {
        SearchResult result = openLibraryClient.search(query, limit);
        return new ExternalBookSearchResponse(query, result.totalResults(), result.books());
    }

    @Transactional
    public BookResponse importBook(ExternalBookImportRequest request) {
        return findExistingBook(request)
            .map(bookService::toResponse)
            .orElseGet(() -> bookService.toResponse(bookRepository.save(toBook(request))));
    }

    private java.util.Optional<Book> findExistingBook(ExternalBookImportRequest request) {
        java.util.Optional<Book> byExternalId = bookRepository.findByExternalSourceAndExternalId(
            request.externalSource(),
            request.externalId()
        );

        if (byExternalId.isPresent() || !StringUtils.hasText(request.isbn())) {
            return byExternalId;
        }

        return bookRepository.findByIsbn(request.isbn().trim());
    }

    private Book toBook(ExternalBookImportRequest request) {
        Book book = new Book();
        book.setTitle(request.title().trim());
        book.setAuthors(request.authors().trim());
        book.setIsbn(trimToNull(request.isbn()));
        book.setDescription(trimToNull(request.description()));
        book.setCoverImageUrl(trimToNull(request.coverImageUrl()));
        book.setPublisher(trimToNull(request.publisher()));
        book.setCategories(trimToNull(request.categories()));
        book.setExternalSource(request.externalSource().trim());
        book.setExternalId(request.externalId().trim());
        book.setPublishedDate(request.publishedDate());
        book.setPageCount(request.pageCount());
        return book;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
