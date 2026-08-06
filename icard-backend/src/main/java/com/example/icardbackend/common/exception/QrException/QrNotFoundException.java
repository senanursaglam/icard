package com.example.icardbackend.common.exception.QrException;

public class QrNotFoundException extends RuntimeException {
    public QrNotFoundException(String message) {
        super(message);
    }
}