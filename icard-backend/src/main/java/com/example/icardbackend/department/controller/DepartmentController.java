package com.example.icardbackend.department.controller;

import com.example.icardbackend.department.dto.DepartmentRequest;
import com.example.icardbackend.department.dto.DepartmentResponse;
import com.example.icardbackend.department.dto.DepartmentStatsResponse;
import com.example.icardbackend.department.entity.Department;
import com.example.icardbackend.department.mapper.DepartmentMapper;
import com.example.icardbackend.department.service.DepartmentService;
import com.example.icardbackend.security.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentMapper departmentMapper;

    @PostMapping("/api/admin/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        Department department = departmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentMapper.toResponse(department));
    }

    @PutMapping("/api/admin/departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponse> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        Department department = departmentService.update(id, request);
        return ResponseEntity.ok(departmentMapper.toResponse(department));
    }

    @GetMapping("/api/departments")
    public List<DepartmentResponse> getAll() {
        return departmentService.getAll().stream().map(departmentMapper::toResponse).toList();
    }

    @GetMapping("/api/departments/{id}/stats")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public DepartmentStatsResponse getStats(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return departmentService.getStats(id, userDetails.getUser());
    }
}