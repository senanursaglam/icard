package com.example.icardbackend.leave.dto;

import com.example.icardbackend.leave.entity.LeaveStatus;
import com.example.icardbackend.leave.entity.LeaveType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestResponse {

    private Long id;
    private String userFullName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;
    private String reason;
    private LeaveStatus status;
    private String approvedByFullName;
    private LocalDateTime createdDate;
    private LocalDateTime decisionDate;
}