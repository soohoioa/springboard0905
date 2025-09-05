package com.project.board0905.domain.user.error;

import com.project.board0905.common.error.BaseErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-404", "존재하지 않는 사용자입니다."),
    USERNAME_DUPLICATE(HttpStatus.CONFLICT, "USER-409-USERNAME", "이미 사용 중인 사용자명입니다."),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "USER-409-EMAIL", "이미 사용 중인 이메일입니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    @Override public HttpStatus getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getDefaultMessage() { return defaultMessage; }
}
