package com.careerforge.controller;

import com.careerforge.dto.DashboardStatsResponse;
import com.careerforge.service.DashboardStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard-stats")
public class DashboardStatsController {

    private final DashboardStatsService dashboardStatsService;

    public DashboardStatsController(
            DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<DashboardStatsResponse> getStats(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                dashboardStatsService.getStats(userId)
        );
    }
}