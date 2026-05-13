package com.shelfaware.repository;

import com.shelfaware.domain.Book;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    Optional<Book> findByExternalSourceAndExternalId(String externalSource, String externalId);

    Page<Book> findByTitleContainingIgnoreCaseOrAuthorsContainingIgnoreCase(
        String title,
        String authors,
        Pageable pageable
    );
}
