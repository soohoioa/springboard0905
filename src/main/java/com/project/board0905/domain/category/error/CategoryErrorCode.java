package com.project.board0905.domain.category.error;

import com.project.board0905.common.error.BaseErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CategoryErrorCode implements BaseErrorCode {
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CAT-404", "존재하지 않는 카테고리입니다."),
    CATEGORY_NAME_DUPLICATE(HttpStatus.CONFLICT, "CAT-409-NAME", "이미 존재하는 카테고리명입니다."),
    CATEGORY_SLUG_DUPLICATE(HttpStatus.CONFLICT, "CAT-409-SLUG", "이미 존재하는 슬러그입니다."),
    CATEGORY_SLUG_INVALID(HttpStatus.BAD_REQUEST, "CAT-400-SLUG", "유효한 slug를 생성할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    @Override public HttpStatus getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getDefaultMessage() { return defaultMessage; }
}
