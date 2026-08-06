package com.example.icardbackend.common.exception.AttendanceException;

public class NoActiveAttendanceException extends RuntimeException {
    public NoActiveAttendanceException(String message) {
        super(message);
    }
}
