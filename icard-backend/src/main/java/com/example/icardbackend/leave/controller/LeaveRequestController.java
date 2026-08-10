package com.example.icardbackend.leave.controller;

import com.example.icardbackend.common.dto.PageResponse;
import com.example.icardbackend.leave.dto.LeaveRequestCreateRequest;
import com.example.icardbackend.leave.dto.LeaveRequestResponse;
import com.example.icardbackend.leave.entity.LeaveRequest;
import com.example.icardbackend.leave.mapper.LeaveRequestMapper;
import com.example.icardbackend.leave.service.LeaveRequestService;
import com.example.icardbackend.security.userdetails.CustomUserDetails;
import com.example.icardbackend.user.entity.User;
import com.example.icardbackend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final LeaveRequestMapper leaveRequestMapper;
    private final UserService userService;

    //
    @PostMapping("/api/leaves")
    public ResponseEntity<LeaveRequestResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LeaveRequestCreateRequest request) {

        LeaveRequest leaveRequest = leaveRequestService.create(userDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestMapper.toResponse(leaveRequest));
    }

    @GetMapping("/api/leaves/me")
    public List<LeaveRequestResponse> getMyLeaves(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return leaveRequestService.getMyLeaves(userDetails.getUser()).stream()
                .map(leaveRequestMapper::toResponse)
                .toList();
    }

    @GetMapping("/api/manager/leaves")
    @PreAuthorize("hasRole('MANAGER')")
    public PageResponse<LeaveRequestResponse> getTeamLeaves(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<User> team = userService.getTeamEntities(userDetails.getUser());
        Page<LeaveRequest> leavePage = leaveRequestService.getTeamLeaves(team, page, size);
        return PageResponse.from(leavePage.map(leaveRequestMapper::toResponse));
    }

    @PutMapping("/api/manager/leaves/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LeaveRequestResponse> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        LeaveRequest leaveRequest = leaveRequestService.approve(id, userDetails.getUser());
        return ResponseEntity.ok(leaveRequestMapper.toResponse(leaveRequest));
    }

    @PutMapping("/api/manager/leaves/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LeaveRequestResponse> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        LeaveRequest leaveRequest = leaveRequestService.reject(id, userDetails.getUser());
        return ResponseEntity.ok(leaveRequestMapper.toResponse(leaveRequest));
    }
}