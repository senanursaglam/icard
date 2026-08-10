package com.example.icardbackend.user.dto;

import com.example.icardbackend.user.entity.RoleType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserUpdateRequest {

    private String firstName;
    private String lastName;
    private RoleType role;
    private Long departmentId;
    private Long managerId;
}
