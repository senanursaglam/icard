package com.example.icardbackend.attendance.mapper;

import com.example.icardbackend.attendance.dto.AttendanceResponse;
import com.example.icardbackend.attendance.entity.Attendance;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {
    public AttendanceResponse toAttendanceResponse (Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .userFullName(attendance.getUser().getFirstName() + " " + attendance.getUser().getLastName())
                .loginTime(attendance.getLoginTime())
                .logoutTime(attendance.getLogoutTime())
                .workedMinutes(attendance.getWorkedMinutes())
                .workedHours(attendance.getWorkedHours())
                .status(attendance.getStatus())
                .isLate(attendance.getIsLate())
                .overtimeMinutes(attendance.getOvertimeMinutes())
                .isEarlyLeave(attendance.getIsEarlyLeave())
                .build();


    }
}
