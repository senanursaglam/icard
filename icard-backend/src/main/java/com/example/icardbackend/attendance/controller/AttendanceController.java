package com.example.icardbackend.attendance.controller;

import com.example.icardbackend.attendance.dto.ActiveStatusResponse;
import com.example.icardbackend.attendance.dto.AttendanceFilterRequest;
import com.example.icardbackend.attendance.dto.AttendanceResponse;
import com.example.icardbackend.attendance.entity.Attendance;
import com.example.icardbackend.attendance.mapper.AttendanceMapper;
import com.example.icardbackend.attendance.service.AttendanceService;
import com.example.icardbackend.common.dto.PageResponse;
import com.example.icardbackend.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceMapper attendanceMapper;

    @PostMapping("/checkout")
    public ResponseEntity<AttendanceResponse> checkOut(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Attendance attendance = attendanceService.checkOut(userDetails.getUser());
        return ResponseEntity.ok(attendanceMapper.toAttendanceResponse(attendance));
    }

    // server-side pagination
    @GetMapping
    public ResponseEntity<PageResponse<AttendanceResponse>> getHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {

        Page<Attendance> attendancePage = attendanceService.getAttendanceHistory(userDetails.getUser(), pageable);
        Page<AttendanceResponse> responsePage = attendancePage.map(attendanceMapper::toAttendanceResponse);

        return ResponseEntity.ok(PageResponse.from(responsePage));
    }
    @PostMapping("/filter")
    public ResponseEntity<PageResponse<AttendanceResponse>> filter(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AttendanceFilterRequest request) {

        Page<Attendance> page = attendanceService.filterAttendance(userDetails.getUser(), request);
        Page<AttendanceResponse> responsePage = page.map(attendanceMapper::toAttendanceResponse);

        return ResponseEntity.ok(PageResponse.from(responsePage));
    }
    @GetMapping("/active-status")
    public ActiveStatusResponse getActiveStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean active =attendanceService.hasActiveAttendance(userDetails.getUser());
        return ActiveStatusResponse.builder().active(active).build();
    }
}