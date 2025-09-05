package com.project.board0905.domain.like.error;

import com.project.board0905.common.error.BaseErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum LikeErrorCode implements BaseErrorCode {
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "LIKE-404", "존재하지 않는 좋아요입니다."),
    ALREADY_LIKED(HttpStatus.CONFLICT, "LIKE-409-ALREADY", "이미 좋아요한 상태입니다."),
    NOT_LIKED(HttpStatus.CONFLICT, "LIKE-409-NOT", "좋아요 상태가 아닙니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    @Override public HttpStatus getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getDefaultMessage() { return defaultMessage; }
}
