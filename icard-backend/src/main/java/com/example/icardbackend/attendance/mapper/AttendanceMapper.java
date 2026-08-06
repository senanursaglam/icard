package com.example.icardbackend.attendance.mapper;

import com.example.icardbackend.attendance.dto.AttendanceResponse;
import com.example.icardbackend.attendance.entity.Attendance;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {
    public AttendanceResponse toAttendanceResponse (Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .loginTime(attendance.getLoginTime())
                .logoutTime(attendance.getLogoutTime())
                .workedMinutes(attendance.getWorkedMinutes())
                .workedHours(attendance.getWorkedHours())
                .status(attendance.getStatus())
                .build();


    }
}
