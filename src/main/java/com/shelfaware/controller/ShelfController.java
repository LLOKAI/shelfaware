package com.shelfaware.controller;

import com.shelfaware.api.shelf.ShelfItemRequest;
import com.shelfaware.api.shelf.ShelfItemResponse;
import com.shelfaware.domain.ReadingStatus;
import com.shelfaware.security.CustomUserPrincipal;
import com.shelfaware.service.ShelfService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/shelf")
public class ShelfController {

    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    @GetMapping
    public List<ShelfItemResponse> getShelf(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestParam(required = false) ReadingStatus status
    ) {
        return shelfService.getShelf(principal.getId(), status);
    }

    @PutMapping("/{bookId}")
    public ShelfItemResponse upsertShelfItem(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @PathVariable Long bookId,
        @Valid @RequestBody ShelfItemRequest request
    ) {
        return shelfService.upsertShelfItem(principal.getId(), bookId, request);
    }
}
