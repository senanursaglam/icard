package com.example.icardbackend.attendance.dto;

import com.example.icardbackend.attendance.entity.AttendanceStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {

    private Long id;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private Long workedMinutes;
    private Double workedHours;
    private AttendanceStatus status;
    private Boolean isLate;
    private Long overtimeMinutes;
    private String userFullName;
    private Boolean isEarlyLeave;
}