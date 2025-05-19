package com.quartzbatchcontrol.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 인증 관련 에러
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호는 영문 대소문자, 숫자, 특수문자를 포함한 8자리 이상이어야 합니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "유효하지 않은 이메일 형식입니다."),
    INVALID_NAME(HttpStatus.BAD_REQUEST, "이름은 2자 이상 20자 이하여야 합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "계정이 비활성화되었습니다."),
    ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "계정이 잠겼습니다."),
    CREDENTIALS_EXPIRED(HttpStatus.UNAUTHORIZED, "비밀번호 유효기간이 만료되었습니다."),

    // Auth
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "Authentication failed"),

    // 토큰 관련 에러
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),

    // 사용자 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_USER(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자입니다."),

    // 서버 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 공통 예외
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "필수 파라미터가 누락되었습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "파라미터 타입이 올바르지 않습니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "엔티티를 찾을 수 없습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청값이 유효하지 않습니다."),
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "잘못된 인자입니다."),
    QUARTZ_SCHEDULING_FAILED(HttpStatus.BAD_REQUEST, "Job 스케줄링 중 오류가 발생했습니다."),
    INVALID_JOB_CLASS(HttpStatus.BAD_REQUEST, "존재하지 않는 Job 입니다."),
    INVALID_CRON_EXPRESSION(HttpStatus.BAD_REQUEST, "잘못된 Cron 표현식입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
