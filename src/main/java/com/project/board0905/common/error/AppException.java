package com.project.board0905.common.error;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final BaseErrorCode errorCode;

    public AppException(BaseErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }
    public AppException(BaseErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    public AppException(BaseErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
