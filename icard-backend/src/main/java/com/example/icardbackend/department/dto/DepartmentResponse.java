package com.example.icardbackend.department.dto;
import com.example.icardbackend.department.entity.Department;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String name;
    private Long managerId;
    private String managerFullName;
    private LocalDateTime createdDate;

}