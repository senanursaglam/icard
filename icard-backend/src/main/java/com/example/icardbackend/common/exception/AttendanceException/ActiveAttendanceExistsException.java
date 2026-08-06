package com.example.icardbackend.common.exception.AttendanceException;

public class ActiveAttendanceExistsException extends RuntimeException {
    public ActiveAttendanceExistsException(String message) {
        super(message);
    }
}
