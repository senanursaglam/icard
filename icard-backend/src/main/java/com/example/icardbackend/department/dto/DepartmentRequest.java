package com.example.icardbackend.department.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentRequest {

    @NotBlank(message = "Departman adı boş olamaz")
    private String name;

    private Long managerId;
}