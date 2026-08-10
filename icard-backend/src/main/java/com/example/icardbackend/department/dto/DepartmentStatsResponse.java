package com.example.icardbackend.department.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentStatsResponse {
    private Long departmentId;
    private String departmentName;
    private int memberCount;
    private Double weeklyWorkedHours;
    private Double totalWorkedHours;
}