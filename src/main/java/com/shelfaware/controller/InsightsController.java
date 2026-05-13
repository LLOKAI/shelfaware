package com.shelfaware.controller;

import com.shelfaware.api.insights.ReadingInsightsResponse;
import com.shelfaware.domain.UserAccount;
import com.shelfaware.service.InsightsService;
import com.shelfaware.service.UserService;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/insights")
public class InsightsController {

    private final InsightsService insightsService;
    private final UserService userService;

    public InsightsController(InsightsService insightsService, UserService userService) {
        this.insightsService = insightsService;
        this.userService = userService;
    }

    @GetMapping
    public ReadingInsightsResponse getInsights(Principal principal) {
        UserAccount user = userService.getByUsername(principal.getName());
        return insightsService.getInsights(user.getId());
    }
}
