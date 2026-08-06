package com.example.icardbackend.user.dto;

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
}