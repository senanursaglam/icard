package com.example.icardbackend.user.dto;

import com.example.icardbackend.attendance.dto.AttendanceResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDetailResponse {
    private UserResponse user;
    private List<AttendanceResponse> recentAttendance;
    private double totalOvertimeHours;
    private long lateCount;
    private int pendingLeaveCount;
}