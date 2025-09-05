package com.project.board0905.common.error;

public class BusinessException extends AppException {
    public BusinessException(String message) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}
