package com.example.icardbackend.qr.service;

import com.google.zxing.WriterException;
import com.example.icardbackend.qr.util.QrImageGenerator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;
import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final QrImageGenerator qrImageGenerator;

    public void sendQrCodeMail(String toEmail, String qrCodeContent) {

        try {
            byte[] qrImageBytes = qrImageGenerator.generateQrImage(qrCodeContent);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true = multipart, image embed için gerekli

            helper.setTo(toEmail);
            helper.setSubject("ICard iNFODİF - Giriş QR Kodunuz");
            helper.setText(
                    "<h3>Giriş için QR kodunuzu okutun</h3>" +
                            "<p>Bu kod 2 dakika geçerlidir.</p>" +
                            "<img src='cid:qrCodeImage' />",
                    true // içeriğin HTML olduğunu belirtir
            );

            // 'cid:qrCodeImage' referansı, embed edilen imagein idsiyle eşleşmeli
            helper.addInline("qrCodeImage", new ByteArrayResource(qrImageBytes), "image/png");
            //ek olarak değil direkt embed yolladığımızdan addInLine kullanılır

            mailSender.send(message);

        } catch (MessagingException | WriterException | IOException e) {
            throw new RuntimeException("QR kod maili gönderilemedi: " + e.getMessage(), e);
        }
    }
    public void sendLeaveRequestNotificationToManager(String managerEmail, String employeeFullName,
                                                      LocalDate startDate, LocalDate endDate) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(managerEmail);
            helper.setSubject("Yeni İzin Talebi - " + employeeFullName);
            helper.setText(
                    "<h3>Yeni bir izin talebi onayınızı bekliyor</h3>" +
                            "<p><b>Personel:</b> " + employeeFullName + "</p>" +
                            "<p><b>Tarih Aralığı:</b> " + startDate + " - " + endDate + "</p>",
                    true
            );

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("İzin talebi maili gönderilemedi: " + e.getMessage(), e);
        }
    }

    public void sendLeaveDecisionNotification(String employeeEmail, boolean approved,
                                              LocalDate startDate, LocalDate endDate) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String statusText = approved ? "onaylandı" : "reddedildi";

            helper.setTo(employeeEmail);
            helper.setSubject("İzin Talebiniz " + (approved ? "Onaylandı" : "Reddedildi"));
            helper.setText(
                    "<h3>İzin talebiniz " + statusText + "</h3>" +
                            "<p><b>Tarih Aralığı:</b> " + startDate + " - " + endDate + "</p>",
                    true
            );

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("İzin karar maili gönderilemedi: " + e.getMessage(), e);
        }
    }
}