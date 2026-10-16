package com.capstone.backend.global.exception;

/**
 * 공통 에러 응답. {"code": "NOT_FOUND", "message": "프로젝트를 찾을 수 없습니다."}
 * message 필드는 기존 그대로 두어 error.response.data.message를 쓰는 프론트 코드가 계속 동작한다.
 */
public record ErrorResponse(String code, String message) {

    public static ErrorResponse of(ErrorCode code, String message) {
        return new ErrorResponse(code.name(), message);
    }
}
