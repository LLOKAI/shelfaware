package com.shelfaware.controller;

import com.shelfaware.api.journey.JourneyResponse;
import com.shelfaware.api.journey.ReadingGoalRequest;
import com.shelfaware.api.journey.ReadingGoalResponse;
import com.shelfaware.domain.UserAccount;
import com.shelfaware.service.JourneyService;
import com.shelfaware.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class JourneyController {
    private final JourneyService journeyService;
    private final UserService userService;

    public JourneyController(JourneyService journeyService, UserService userService) {
        this.journeyService = journeyService;
        this.userService = userService;
    }

    @GetMapping("/journey")
    public JourneyResponse getJourney(Principal principal, @RequestParam(required = false) Integer year) {
        int selectedYear = year == null ? LocalDate.now().getYear() : year;
        return journeyService.getJourney(user(principal).getId(), selectedYear);
    }

    @GetMapping("/reading-goals/{year}")
    public ReadingGoalResponse getGoal(Principal principal, @PathVariable int year) {
        return journeyService.getGoal(user(principal).getId(), year);
    }

    @PutMapping("/reading-goals/{year}")
    public ReadingGoalResponse saveGoal(
        Principal principal,
        @PathVariable int year,
        @Valid @RequestBody ReadingGoalRequest request
    ) {
        return journeyService.saveGoal(user(principal).getId(), year, request);
    }

    @DeleteMapping("/reading-sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void undoSession(Principal principal, @PathVariable Long sessionId) {
        journeyService.undoSession(user(principal).getId(), sessionId);
    }

    private UserAccount user(Principal principal) {
        return userService.getByUsername(principal.getName());
    }
}
