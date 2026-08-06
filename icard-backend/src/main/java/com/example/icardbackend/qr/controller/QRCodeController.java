package com.example.icardbackend.qr.controller;

import com.example.icardbackend.attendance.dto.AttendanceResponse;
import com.example.icardbackend.attendance.entity.Attendance;
import com.example.icardbackend.attendance.mapper.AttendanceMapper;
import com.example.icardbackend.attendance.service.AttendanceService;
import com.example.icardbackend.qr.dto.QRCodeResponse;
import com.example.icardbackend.qr.dto.QRVerifyRequest;
import com.example.icardbackend.qr.entity.QRCode;
import com.example.icardbackend.qr.mapper.QRCodeMapper;
import com.example.icardbackend.qr.service.QRCodeService;
import com.example.icardbackend.security.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QRCodeController {

    private final QRCodeService qrCodeService;
    private final QRCodeMapper qrCodeMapper;
    private final AttendanceService attendanceService;
    private final AttendanceMapper attendanceMapper;

    @PostMapping("/generate")
    public ResponseEntity<QRCodeResponse> generate(@AuthenticationPrincipal CustomUserDetails userDetails) {
        QRCode qrCode = qrCodeService.generateQrCode(userDetails.getUser());
        return ResponseEntity.ok(qrCodeMapper.toResponse(qrCode));
    }

    @PostMapping("/verify")
    public ResponseEntity<AttendanceResponse> verify(@Valid @RequestBody QRVerifyRequest request) {
        QRCode verifiedQr = qrCodeService.verifyQrCode(request.getCode());
        Attendance attendance = attendanceService.checkIn(verifiedQr.getUser());
        return ResponseEntity.ok(attendanceMapper.toAttendanceResponse(attendance));
    }
}