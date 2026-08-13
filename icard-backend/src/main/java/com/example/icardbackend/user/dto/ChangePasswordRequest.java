package com.example.icardbackend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır.")
    private String newPassword;
}