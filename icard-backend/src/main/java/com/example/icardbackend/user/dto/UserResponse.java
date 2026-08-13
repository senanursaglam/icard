package com.example.icardbackend.user.dto;

import com.example.icardbackend.user.entity.RoleType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse { //frontende dönecek kullanıcı bilgisi

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdDate;
    private RoleType role;
    private String departmentName;
    private String managerName;
    private Boolean active;
    private Boolean temporaryPassword;
}