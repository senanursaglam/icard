package com.example.icardbackend.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse { // login başarılı olunca dönecek jwt ve user bilgisi

    private String token;
    private UserResponse user;
}