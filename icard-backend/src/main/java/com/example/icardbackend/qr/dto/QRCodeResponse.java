package com.example.icardbackend.qr.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRCodeResponse {
//frontendde bunların gözükmesi yeterli
    private String code;
    private LocalDateTime expireDate;
}