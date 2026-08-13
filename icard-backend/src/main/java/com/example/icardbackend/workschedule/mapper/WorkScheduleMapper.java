package com.example.icardbackend.workschedule.mapper;

import com.example.icardbackend.workschedule.dto.WorkScheduleResponse;
import com.example.icardbackend.workschedule.entity.WorkSchedule;
import org.springframework.stereotype.Component;

@Component
public class WorkScheduleMapper {

    public WorkScheduleResponse toResponse(WorkSchedule ws) {
        return WorkScheduleResponse.builder()
                .id(ws.getId())
                .departmentId(ws.getDepartment() != null ? ws.getDepartment().getId() : null)
                .departmentName(ws.getDepartment() != null ? ws.getDepartment().getName() : "Genel")
                .startTime(ws.getStartTime())
                .endTime(ws.getEndTime())
                .toleranceMinutes(ws.getToleranceMinutes())
                .build();
    }
}