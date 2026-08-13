package com.example.icardbackend.attendance.controller;

import com.example.icardbackend.attendance.dto.DailyHoursResponse;
import com.example.icardbackend.attendance.dto.DashboardStatsResponse;
import com.example.icardbackend.attendance.dto.WeeklyChartResponse;
import com.example.icardbackend.attendance.service.DashboardService;
import com.example.icardbackend.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStatsResponse getStats(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return dashboardService.getStats(userDetails.getUser());
    }

    @GetMapping("/weekly-chart")
    public WeeklyChartResponse getWeeklyChart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int weekOffset) {
        return dashboardService.getWeeklyChart(userDetails.getUser(), weekOffset);
    }

    @GetMapping("/department/{id}/weekly-chart")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public WeeklyChartResponse getDepartmentWeeklyChart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int weekOffset) {
        return dashboardService.getWeeklyChartForDepartment(id, weekOffset);
    }
}