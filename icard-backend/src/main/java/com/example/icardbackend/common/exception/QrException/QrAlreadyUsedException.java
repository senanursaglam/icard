package com.example.icardbackend.common.exception.QrException;

public class QrAlreadyUsedException extends RuntimeException {
    public QrAlreadyUsedException(String message) {
        super(message);
    }
}