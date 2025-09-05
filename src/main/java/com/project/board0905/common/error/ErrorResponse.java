package com.project.board0905.common.error;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        int status,
        String message,
        String path,
        String traceId,
        List<FieldError> errors,
        OffsetDateTime timestamp
) {
    public static ErrorResponse of(ErrorCode code, String message, String path, String traceId, List<FieldError> errors) {
        return new ErrorResponse(
                code.code,
                code.status.value(),
                message != null ? message : code.defaultMessage,
                path,
                traceId,
                errors,
                OffsetDateTime.now()
        );
    }

    public record FieldError(String field, String reason, Object rejectedValue) {
        public static FieldError of(String field, String reason, Object rejectedValue) {
            return new FieldError(field, reason, rejectedValue);
        }
    }
}
