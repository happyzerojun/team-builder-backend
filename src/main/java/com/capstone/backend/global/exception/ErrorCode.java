package com.capstone.backend.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러 응답의 기계 판독용 코드.
 * 프론트는 사용자에게 보여 줄 문구로 message를, 분기 처리에는 code를 쓴다.
 * 이전에는 {"message": "..."}만 있어 클라이언트가 문구 비교로 에러 종류를 구분해야 했다.
 */
public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
    CONFLICT(HttpStatus.CONFLICT),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
