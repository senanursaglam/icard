package com.example.icardbackend.attendance.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyHoursResponse {
    private String day;
    private Double hours;
}