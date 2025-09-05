package com.project.board0905.common.web;

import com.project.board0905.common.error.BaseErrorCode;
import com.project.board0905.common.error.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommonApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final Integer status;     // HTTP status (숫자)
    private final T data;
    private final LocalDateTime timestamp;

    @Builder
    private CommonApiResponse(boolean success, String code, String message, Integer status, T data, LocalDateTime timestamp) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.status = status;
        this.data = data;
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
    }

    /* ------------ 성공 응답 ------------ */
    public static <T> CommonApiResponse<T> ok(T data) {
        return CommonApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message("요청이 성공적으로 처리되었습니다.")
                .status(200)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    public static CommonApiResponse<Void> ok() { return ok(null); }

    /* ------------ 실패 응답 ------------ */
    // 1) 표준 에러코드 기반
    public static <T> CommonApiResponse<T> fail(BaseErrorCode errorCode) {
        return CommonApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getDefaultMessage())  // <- 여기 수정
                .status(errorCode.getStatus().value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 2) 표준 에러코드 + 커스텀 메시지(오버라이드)
    public static <T> CommonApiResponse<T> fail(BaseErrorCode errorCode, String overrideMessage) {
        return CommonApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message((overrideMessage != null && !overrideMessage.isBlank())
                        ? overrideMessage : errorCode.getDefaultMessage())
                .status(errorCode.getStatus().value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 3) 표준 에러코드 + data
    public static <T> CommonApiResponse<T> fail(BaseErrorCode errorCode, T data) {
        return CommonApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getDefaultMessage())  // <- 여기 수정
                .status(errorCode.getStatus().value())
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 4) 임의 코드/메시지(+선택적 status 지정)
    public static <T> CommonApiResponse<T> fail(String code, String message) {
        return fail(code, message, ErrorCode.INVALID_ARGUMENT.getStatus().value());
    }
    public static <T> CommonApiResponse<T> fail(String code, String message, int status) {
        return CommonApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> CommonApiResponse<T> error(String code, String message, int status) {
        return CommonApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .status(status)
                .data(null)
                .build();
    }

    public static <T> CommonApiResponse<T> error(BaseErrorCode ec) {
        return error(ec.getCode(), ec.getDefaultMessage(), ec.getStatus().value());
    }

    public static <T> CommonApiResponse<T> error(BaseErrorCode ec, String overrideMessage) {
        String msg = (overrideMessage == null || overrideMessage.isBlank())
                ? ec.getDefaultMessage() : overrideMessage;
        return error(ec.getCode(), msg, ec.getStatus().value());
    }
}