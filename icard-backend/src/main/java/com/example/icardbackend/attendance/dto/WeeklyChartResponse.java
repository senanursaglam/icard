package com.example.icardbackend.attendance.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyChartResponse {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private List<DailyHoursResponse> days;
}