package com.example.icardbackend.common.exception.LeaveRequestException;

public class LeaveAlreadyDecidedException extends RuntimeException {
    public LeaveAlreadyDecidedException(String message) { super(message); }
}