package com.example.icardbackend.qr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRVerifyRequest {
//webcam ile okutulan QRın string olarak backend'e gönderileceği istek gövdesi.
    @NotBlank(message = "QR kodu boş olamaz")
    private String code;
}