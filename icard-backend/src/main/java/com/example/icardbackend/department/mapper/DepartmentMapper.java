package com.example.icardbackend.department.mapper;

import com.example.icardbackend.department.dto.DepartmentResponse;
import com.example.icardbackend.department.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .managerId(department.getManager() != null ? department.getManager().getId() : null)
                .managerFullName(department.getManager() != null
                        ? department.getManager().getFirstName() + " " + department.getManager().getLastName()
                        : null)
                .createdDate(department.getCreatedDate())
                .build();
    }
}