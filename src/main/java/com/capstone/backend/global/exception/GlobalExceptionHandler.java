package com.capstone.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static ResponseEntity<ErrorResponse> respond(ErrorCode code, String message) {
        return ResponseEntity.status(code.status()).body(ErrorResponse.of(code, message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("잘못된 요청입니다.");
        return respond(ErrorCode.VALIDATION_FAILED, message);
    }

    // 깨진 JSON 본문, 숫자 자리에 문자열, 필수 쿼리 파라미터 누락은 클라이언트 잘못이다.
    // 이전에는 catch-all에 걸려 500으로 응답했다.
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        return respond(ErrorCode.VALIDATION_FAILED, "요청 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException e) {
        return respond(ErrorCode.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException e) {
        return respond(ErrorCode.UNAUTHORIZED, e.getMessage());
    }

    // 컨트롤러 안에서 던져진 Spring Security 예외는 필터가 아닌 여기로 온다.
    // 별도 핸들러가 없으면 아래 Exception 핸들러가 잡아 500이 된다.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e) {
        return respond(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException e) {
        return respond(ErrorCode.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return respond(ErrorCode.FORBIDDEN, "권한이 없습니다.");
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        return respond(ErrorCode.NOT_FOUND, e.getMessage());
    }

    // 없는 URL. Spring 6.1부터 NoResourceFoundException이 던져져 catch-all에서 500이 되던 것을 404로 돌린다.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
        return respond(ErrorCode.NOT_FOUND, "요청한 주소를 찾을 수 없습니다.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return respond(ErrorCode.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다.");
    }

    // 서비스 계층의 검증 실패(잘못된 값) → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return respond(ErrorCode.VALIDATION_FAILED, e.getMessage());
    }

    // 상태 규칙 위반(이미 처리된 지원서, 정원 초과 등) → 409
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return respond(ErrorCode.CONFLICT, e.getMessage());
    }

    // 유니크 제약 위반(동시 중복 지원 등) → 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: {}", e.getMostSpecificCause().getMessage());
        return respond(ErrorCode.CONFLICT, "이미 처리된 요청이거나 중복된 데이터입니다.");
    }

    // 행 잠금 대기 시간 초과·데드락 감지 → 잠시 후 재시도하면 되는 충돌이라 409
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleLockFailure(PessimisticLockingFailureException e) {
        log.warn("Lock failure: {}", e.getMostSpecificCause().getMessage());
        return respond(ErrorCode.CONFLICT, "다른 요청과 충돌했습니다. 잠시 후 다시 시도해주세요.");
    }

    // 예상하지 못한 예외: 원인은 로그에만 남기고 응답에는 내부 메시지를 넣지 않는다.
    // 이전 구현은 Throwable(Error 포함)을 잡아 e.getMessage()를 그대로 응답해 SQL 오류 등 내부 정보가 노출될 수 있었다 (TS-02)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Unhandled exception", e);
        return respond(ErrorCode.INTERNAL_ERROR, "서버 내부 오류가 발생했습니다.");
    }
}
