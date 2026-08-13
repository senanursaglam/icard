package com.example.icardbackend.attendance.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamOvertimeSummaryResponse {
    private String userFullName;
    private double totalOvertimeHours;
    private long lateCount;
}