package com.capstone.backend.global.exception;

/** 요청한 리소스가 없음 → 404 NOT_FOUND */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
