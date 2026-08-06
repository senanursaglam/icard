package com.example.icardbackend.qr.service;

import com.example.icardbackend.common.exception.QrException.QrAlreadyUsedException;
import com.example.icardbackend.common.exception.QrException.QrExpiredException;
import com.example.icardbackend.common.exception.QrException.QrNotFoundException;
import com.example.icardbackend.qr.entity.QRCode;
import com.example.icardbackend.qr.repository.QRCodeRepository;
import com.example.icardbackend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QRCodeService {

    private final QRCodeRepository qrCodeRepository;
    private final MailService mailService;

    private static final int EXPIRATION_MINUTES = 2;

    // yeni qr üretir, eğer kullanıcının eski qrı varsa onu inactive yapar
    public QRCode generateQrCode(User user) {

        List<QRCode> oldActiveQrs = qrCodeRepository.findAllByUserAndActiveTrue(user);

        // Eğer 10 saniye içinde üretilmiş bir aktif QR varsa, yenisini üretme, onu döndür
        for (QRCode existing : oldActiveQrs) {
            if (existing.getCreatedDate().isAfter(LocalDateTime.now().minusSeconds(10))) {
                return existing;
            }
        }

        for (QRCode oldQr : oldActiveQrs) {
            oldQr.setActive(false);
        }
        qrCodeRepository.saveAll(oldActiveQrs);

        QRCode qrCode = QRCode.builder()
                .code(UUID.randomUUID().toString())
                .user(user)
                .createdDate(LocalDateTime.now())
                .expireDate(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .used(false)
                .active(true)
                .build();

        QRCode savedQrCode = qrCodeRepository.save(qrCode);

        mailService.sendQrCodeMail(user.getEmail(), savedQrCode.getCode());

        return savedQrCode;
    }
    // qr geçerliyse used=true yapar ve qrı döner
    // (Attendance kaydı bu QR'dan üretilecek)
    public QRCode verifyQrCode(String code) {

        QRCode qrCode = qrCodeRepository.findByCodeAndActiveTrue(code)//bu userın şu anki active qrını bulur
                .orElseThrow(() -> new QrNotFoundException("Geçersiz QR kodu."));

        if (Boolean.TRUE.equals(qrCode.getUsed())) {
            throw new QrAlreadyUsedException("QR kodu zaten kullanılmış.");
        }

        if (qrCode.getExpireDate().isBefore(LocalDateTime.now())) {
            throw new QrExpiredException("QR kodunun süresi dolmuş.");
        }

        qrCode.setUsed(true);//qr geçerli
        return qrCodeRepository.save(qrCode);
    }
}