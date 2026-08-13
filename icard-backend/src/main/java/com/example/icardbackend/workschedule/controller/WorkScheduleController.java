package com.example.icardbackend.workschedule.controller;

import com.example.icardbackend.workschedule.dto.WorkScheduleRequest;
import com.example.icardbackend.workschedule.dto.WorkScheduleResponse;
import com.example.icardbackend.workschedule.entity.WorkSchedule;
import com.example.icardbackend.workschedule.mapper.WorkScheduleMapper;
import com.example.icardbackend.workschedule.service.WorkScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;
    private final WorkScheduleMapper workScheduleMapper;

    @PostMapping("/api/admin/work-schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkScheduleResponse> create(@Valid @RequestBody WorkScheduleRequest request) {
        WorkSchedule ws = workScheduleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(workScheduleMapper.toResponse(ws));
    }

    @PutMapping("/api/admin/work-schedules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkScheduleResponse> update(@PathVariable Long id, @Valid @RequestBody WorkScheduleRequest request) {
        WorkSchedule ws = workScheduleService.update(id, request);
        return ResponseEntity.ok(workScheduleMapper.toResponse(ws));
    }

    @GetMapping("/api/work-schedules")
    public List<WorkScheduleResponse> getAll() {
        return workScheduleService.getAll().stream().map(workScheduleMapper::toResponse).toList();
    }
}