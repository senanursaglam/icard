package com.example.icardbackend.common.exception.QrException;

public class QrExpiredException extends RuntimeException {
    public QrExpiredException(String message) {
        super(message);
    }
}