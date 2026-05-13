package com.shelfaware.controller;

import com.shelfaware.api.shelf.ShelfItemRequest;
import com.shelfaware.api.shelf.ShelfItemResponse;
import com.shelfaware.domain.ReadingStatus;
import com.shelfaware.domain.UserAccount;
import com.shelfaware.service.ShelfService;
import com.shelfaware.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
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
    private final UserService userService;

    public ShelfController(ShelfService shelfService, UserService userService) {
        this.shelfService = shelfService;
        this.userService = userService;
    }

    @GetMapping
    public List<ShelfItemResponse> getShelf(
        Principal principal,
        @RequestParam(required = false) ReadingStatus status
    ) {
        UserAccount user = userService.getByUsername(principal.getName());
        return shelfService.getShelf(user.getId(), status);
    }

    @PutMapping("/{bookId}")
    public ShelfItemResponse upsertShelfItem(
        Principal principal,
        @PathVariable Long bookId,
        @Valid @RequestBody ShelfItemRequest request
    ) {
        UserAccount user = userService.getByUsername(principal.getName());
        return shelfService.upsertShelfItem(user.getId(), bookId, request);
    }
}
