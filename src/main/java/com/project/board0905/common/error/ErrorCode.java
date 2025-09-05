package com.project.board0905.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseErrorCode{
    // 400
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C4000", "유효하지 않은 입력값입니다."),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "C4001", "요청 본문을 읽을 수 없습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "C4002", "요청 파라미터 타입이 올바르지 않습니다."),
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "C4003", "잘못된 인자가 전달되었습니다."),

    // 401/403
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C4010", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C4030", "접근 권한이 없습니다."),

    // 404
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C4040", "리소스를 찾을 수 없습니다."),

    // 405
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C4050", "허용되지 않은 HTTP 메서드입니다."),

    // 409
    CONFLICT(HttpStatus.CONFLICT, "C4090", "리소스 충돌이 발생했습니다."),

    // 422
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY, "C4220", "비즈니스 규칙 위반입니다."),

    // 500
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C5000", "내부 서버 오류가 발생했습니다."),
    DB_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C5001", "데이터 처리 중 오류가 발생했습니다.");

    public final HttpStatus status;
    public final String code;
    public final String defaultMessage;

}
