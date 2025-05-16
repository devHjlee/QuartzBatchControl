package com.quartzbatchcontrol.global.exception;

import com.quartzbatchcontrol.global.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("유효하지 않은 요청입니다.");
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", errorMessage));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("[BusinessException] code={}, message={}",
                e.getErrorCode().name(),
                e.getMessage(),
                e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.error(e.getErrorCode()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("ILLEGAL_ARGUMENT", e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.status(ErrorCode.ENTITY_NOT_FOUND.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.ENTITY_NOT_FOUND));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(ErrorCode.ACCESS_DENIED.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.ACCESS_DENIED));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ResponseEntity.status(ErrorCode.MISSING_PARAMETER.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.MISSING_PARAMETER.name(), ErrorCode.MISSING_PARAMETER.getMessage() + ": " + e.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(ErrorCode.TYPE_MISMATCH.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.TYPE_MISMATCH.name(), ErrorCode.TYPE_MISMATCH.getMessage() + ": " + e.getName()));
    }

    // --- Spring Security 인증 관련 추가 예외 ---

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(ErrorCode.INVALID_CREDENTIALS.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INVALID_CREDENTIALS));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(ErrorCode.AUTHENTICATION_FAILED.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.AUTHENTICATION_FAILED));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabledException(DisabledException e) {
        return ResponseEntity.status(ErrorCode.ACCOUNT_DISABLED.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.ACCOUNT_DISABLED));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleLockedException(LockedException e) {
        return ResponseEntity.status(ErrorCode.ACCOUNT_LOCKED.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.ACCOUNT_LOCKED));
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleCredentialsExpiredException(CredentialsExpiredException e) {
        return ResponseEntity.status(ErrorCode.CREDENTIALS_EXPIRED.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.CREDENTIALS_EXPIRED));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled Exception", e);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
