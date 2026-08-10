package com.example.icardbackend.user.dto;
import com.example.icardbackend.user.entity.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserCreateRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private RoleType role;
    private Long departmentId;
    private Long managerId;

}
