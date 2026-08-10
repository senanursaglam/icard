package com.example.icardbackend.common;

import com.example.icardbackend.common.exception.AttendanceException.ActiveAttendanceExistsException;
import com.example.icardbackend.common.exception.AttendanceException.NoActiveAttendanceException;
import com.example.icardbackend.common.exception.DepartmentException.DepartmentAlreadyExistsException;
import com.example.icardbackend.common.exception.DepartmentException.DepartmentNotFoundException;
import com.example.icardbackend.common.exception.UserException.EmailAlreadyExistsException;
import com.example.icardbackend.common.exception.QrException.QrAlreadyUsedException;
import com.example.icardbackend.common.exception.QrException.QrExpiredException;
import com.example.icardbackend.common.exception.QrException.QrNotFoundException;
import com.example.icardbackend.common.exception.UserException.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //o email ile daha önceb ir register yapıldıysa
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex, HttpServletRequest request) {

        ErrorResponse error = buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // kullanıcı bulunamazsa
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest request) {

        ErrorResponse error = buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Spring Security'nin kendi fırlattığı yanlış şifre exceptionı
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        ErrorResponse error = buildError(HttpStatus.UNAUTHORIZED, "Email veya şifre hatalı.", request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // @Valid ile yakalanan validation hataları (RegisterRequest, LoginRequest DTO'larındaki @NotBlank, @Email vb.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("fieldErrors", fieldErrors);
        response.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Beklenmeyen her şey için (catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        ErrorResponse error = buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu.", request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ErrorResponse buildError(HttpStatus status, String message, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
    }

    @ExceptionHandler(QrNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleQrNotFound(
            QrNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(QrAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleQrAlreadyUsed(
            QrAlreadyUsedException ex, HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(QrExpiredException.class)
    public ResponseEntity<ErrorResponse> handleQrExpired(
            QrExpiredException ex, HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.GONE, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.GONE).body(error);
    }

    @ExceptionHandler(ActiveAttendanceExistsException.class)
    public ResponseEntity<ErrorResponse> handleActiveAttendanceExists(
            ActiveAttendanceExistsException ex, HttpServletRequest request){
        ErrorResponse error = buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(NoActiveAttendanceException.class)
    public ResponseEntity<ErrorResponse> handleNoActiveAttendance(
            NoActiveAttendanceException ex, HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.FORBIDDEN, "Bu işlem için yetkiniz yok.", request);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(DepartmentAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleDepartmentAlreadyExists(
            DepartmentAlreadyExistsException ex, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(buildError(HttpStatus.CONFLICT, ex.getMessage(), request));
    }
    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDepartmentNotFound(
            DepartmentNotFoundException ex, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request));
    }
}