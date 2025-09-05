package com.project.board0905.domain.comment.error;

import com.project.board0905.common.error.BaseErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CommentErrorCode implements BaseErrorCode {
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CMT-404", "존재하지 않는 댓글입니다."),
    CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "CMT-400-CONTENT", "내용은 필수입니다."),
    INVALID_DEPTH(HttpStatus.BAD_REQUEST, "CMT-400-DEPTH", "댓글 깊이는 0(루트) 또는 1(대댓글)만 허용됩니다."),
    INVALID_PARENT(HttpStatus.BAD_REQUEST, "CMT-400-PARENT", "부모 댓글이 유효하지 않거나 루트가 아닙니다."),
    BOARD_MISMATCH(HttpStatus.BAD_REQUEST, "CMT-400-BOARD", "부모 댓글과 다른 게시글에는 댓글을 달 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    @Override public HttpStatus getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getDefaultMessage() { return defaultMessage; }
}
