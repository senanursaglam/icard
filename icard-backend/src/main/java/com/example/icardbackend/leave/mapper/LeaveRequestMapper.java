package com.example.icardbackend.leave.mapper;

import com.example.icardbackend.leave.dto.LeaveRequestResponse;
import com.example.icardbackend.leave.entity.LeaveRequest;
import org.springframework.stereotype.Component;

@Component
public class LeaveRequestMapper {

    public LeaveRequestResponse toResponse(LeaveRequest leaveRequest) {
        return LeaveRequestResponse.builder()
                .id(leaveRequest.getId())
                .userFullName(leaveRequest.getUser().getFirstName() + " " + leaveRequest.getUser().getLastName())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .leaveType(leaveRequest.getLeaveType())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus())
                .approvedByFullName(leaveRequest.getApprovedBy() != null
                        ? leaveRequest.getApprovedBy().getFirstName() + " " + leaveRequest.getApprovedBy().getLastName()
                        : null)
                .createdDate(leaveRequest.getCreatedDate())
                .decisionDate(leaveRequest.getDecisionDate())
                .build();
    }
}