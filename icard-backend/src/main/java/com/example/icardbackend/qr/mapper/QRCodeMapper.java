package com.example.icardbackend.qr.mapper;

import com.example.icardbackend.qr.dto.QRCodeResponse;
import com.example.icardbackend.qr.entity.QRCode;
import org.springframework.stereotype.Component;

@Component
public class QRCodeMapper {

    public QRCodeResponse toResponse(QRCode qrCode) {
        return QRCodeResponse.builder()
                .code(qrCode.getCode())
                .expireDate(qrCode.getExpireDate())
                .build();
    }
}