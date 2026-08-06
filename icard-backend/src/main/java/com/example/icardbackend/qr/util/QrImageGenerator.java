package com.example.icardbackend.qr.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class QrImageGenerator {

    private static final int QR_WIDTH = 300;
    private static final int QR_HEIGHT = 300;

    // Verilen string'i (QR kodunu) PNG formatında byte array'e çevirir
    public byte[] generateQrImage(String content) throws WriterException, IOException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }
}