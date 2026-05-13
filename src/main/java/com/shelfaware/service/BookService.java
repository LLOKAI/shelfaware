package com.shelfaware.service;

import com.shelfaware.api.book.BookCreateRequest;
import com.shelfaware.api.book.BookResponse;
import com.shelfaware.domain.Book;
import com.shelfaware.exception.ConflictException;
import com.shelfaware.exception.ResourceNotFoundException;
import com.shelfaware.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> search(String query, Pageable pageable) {
        Page<Book> books = StringUtils.hasText(query)
            ? bookRepository.findByTitleContainingIgnoreCaseOrAuthorsContainingIgnoreCase(query, query, pageable)
            : bookRepository.findAll(pageable);

        return books.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Book getBook(Long id) {
        return bookRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
    }

    @Transactional(readOnly = true)
    public BookResponse getBookResponse(Long id) {
        return toResponse(getBook(id));
    }

    @Transactional
    public BookResponse create(BookCreateRequest request) {
        if (StringUtils.hasText(request.isbn()) && bookRepository.findByIsbn(request.isbn().trim()).isPresent()) {
            throw new ConflictException("A book with that ISBN already exists");
        }

        Book book = new Book();
        book.setTitle(request.title().trim());
        book.setAuthors(request.authors().trim());
        book.setIsbn(trimToNull(request.isbn()));
        book.setDescription(trimToNull(request.description()));
        book.setCoverImageUrl(trimToNull(request.coverImageUrl()));
        book.setPublisher(trimToNull(request.publisher()));
        book.setCategories(trimToNull(request.categories()));
        book.setPublishedDate(request.publishedDate());
        book.setPageCount(request.pageCount());

        return toResponse(bookRepository.save(book));
    }

    public BookResponse toResponse(Book book) {
        return new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getAuthors(),
            book.getIsbn(),
            book.getDescription(),
            book.getCoverImageUrl(),
            book.getPublisher(),
            book.getCategories(),
            book.getExternalSource(),
            book.getExternalId(),
            book.getPublishedDate(),
            book.getPageCount(),
            book.getCreatedAt(),
            book.getUpdatedAt()
        );
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
