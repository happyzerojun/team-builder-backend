package com.capstone.backend.global.exception;

/** 로그인은 했지만 해당 리소스에 대한 권한이 없음(소유자 아님 등) → 403 FORBIDDEN */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
