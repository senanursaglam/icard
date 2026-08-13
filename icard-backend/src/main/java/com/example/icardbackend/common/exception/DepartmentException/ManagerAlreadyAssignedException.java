package com.example.icardbackend.common.exception.DepartmentException;

public class ManagerAlreadyAssignedException extends RuntimeException {
    public ManagerAlreadyAssignedException(String message) { super(message); }
}