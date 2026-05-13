package com.shelfaware.controller;

import com.shelfaware.api.insights.ReadingInsightsResponse;
import com.shelfaware.security.CustomUserPrincipal;
import com.shelfaware.service.InsightsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/insights")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping
    public ReadingInsightsResponse getInsights(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return insightsService.getInsights(principal.getId());
    }
}
