package com.shelfaware.controller;

import com.shelfaware.api.book.BookResponse;
import com.shelfaware.api.book.ExternalBookImportRequest;
import com.shelfaware.api.book.ExternalBookSearchResponse;
import com.shelfaware.service.ExternalBookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/books")
public class ExternalBookController {

    private final ExternalBookService externalBookService;

    public ExternalBookController(ExternalBookService externalBookService) {
        this.externalBookService = externalBookService;
    }

    @GetMapping("/external-search")
    public ExternalBookSearchResponse searchExternalBooks(
        @RequestParam @NotBlank String q,
        @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit
    ) {
        return externalBookService.search(q, limit);
    }

    @PostMapping("/import")
    public BookResponse importBook(@Valid @RequestBody ExternalBookImportRequest request) {
        return externalBookService.importBook(request);
    }
}
