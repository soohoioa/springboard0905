package com.project.board0905.common.error;

public class NotFoundException extends AppException {
    public NotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
