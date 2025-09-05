package com.project.board0905.domain.search.error;

import com.project.board0905.common.error.BaseErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SearchErrorCode implements BaseErrorCode {
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "SRCH-400-DATE", "검색 시작일이 종료일보다 이후일 수 없습니다."),
    UNSUPPORTED_SORT(HttpStatus.BAD_REQUEST, "SRCH-400-SORT", "지원하지 않는 정렬 방식입니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    @Override public HttpStatus getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getDefaultMessage() { return defaultMessage; }
}
