package com.example.icardbackend.workschedule.dto;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleResponse {
    private Long id;
    private Long departmentId;
    private String departmentName;
    private LocalTime startTime;
    private LocalTime endTime;
    private int toleranceMinutes;
}