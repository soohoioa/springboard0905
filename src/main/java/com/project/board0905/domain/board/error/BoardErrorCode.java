package com.project.board0905.domain.board.error;

import com.project.board0905.common.error.BaseErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum BoardErrorCode implements BaseErrorCode {
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD-404", "존재하지 않는 게시글입니다."),
    TITLE_REQUIRED(HttpStatus.BAD_REQUEST, "BOARD-400-TITLE", "제목은 필수입니다."),
    CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "BOARD-400-CONTENT", "내용은 필수입니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    @Override public HttpStatus getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getDefaultMessage() { return defaultMessage; }
}
