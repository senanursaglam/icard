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
}