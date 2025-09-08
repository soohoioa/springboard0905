package com.project.board0905.domain.admin.error;

import com.project.board0905.common.error.BaseErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AdminErrorCode implements BaseErrorCode {
    NOT_ADMIN(HttpStatus.FORBIDDEN, "ADM-403", "관리자 권한이 필요합니다."),
    TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "ADM-404", "대상 리소스를 찾을 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "ADM-400", "유효하지 않은 요청입니다."),
    BOARD_ALREADY_DELETED(HttpStatus.CONFLICT, "ADM-409-BOARD-DEL", "이미 삭제된 게시글입니다."),
    BOARD_NOT_DELETED(HttpStatus.CONFLICT, "ADM-409-BOARD-NOTDEL", "삭제 상태가 아닙니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    @Override public HttpStatus getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getDefaultMessage() { return defaultMessage; }
}
