package com.example.icardbackend.attendance.controller;

import com.example.icardbackend.attendance.dto.AttendanceFilterRequest;
import com.example.icardbackend.attendance.dto.AttendanceResponse;
import com.example.icardbackend.attendance.entity.Attendance;
import com.example.icardbackend.attendance.mapper.AttendanceMapper;
import com.example.icardbackend.attendance.service.AttendanceService;
import com.example.icardbackend.common.dto.PageResponse;
import com.example.icardbackend.security.userdetails.CustomUserDetails;
import com.example.icardbackend.user.dto.UserResponse;
import com.example.icardbackend.user.entity.User;
import com.example.icardbackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

    private final UserService userService;
    private final AttendanceService attendanceService;
    private final AttendanceMapper attendanceMapper;

    @GetMapping("/team")
    public List<UserResponse> getTeam(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.getTeam(userDetails.getUser());
    }

    @PostMapping("/team/attendance")
    public PageResponse<AttendanceResponse> getTeamAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AttendanceFilterRequest request) {

        List<User> team = userService.getTeamEntities(userDetails.getUser());
        Page<Attendance> page = attendanceService.filterTeamAttendance(team, request);
        return PageResponse.from(page.map(attendanceMapper::toAttendanceResponse));
    }
}