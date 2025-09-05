package com.project.board0905.common.error;

import com.project.board0905.common.util.LogUtils;
import com.project.board0905.common.web.CommonApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * 운영에서 상세 메시지 노출 여부 제어
     * application.yml: app.error.expose-details: false (prod), true (local/dev)
     */
    @Value("${app.error.expose-details:false}")
    private boolean exposeDetails;

    private ResponseEntity<Object> build(BaseErrorCode code, String message, HttpServletRequest req,
                                         List<ErrorResponse.FieldError> fields, Throwable ex) {

        // 로그 레벨: AppException 계열은 예상된 흐름이므로 warn, 그 외에는 error
        if (ex instanceof AppException) {
            log.warn("[AppException] code={}, uri={}, msg={}", code.getCode(), req.getRequestURI(), ex.getMessage());
        } else {
            log.error("[UnhandledException] code={}, uri={}, msg={}", code.getCode(), req.getRequestURI(), ex.getMessage(), ex);
        }

        String safeMessage = exposeDetails
                ? (message != null ? message : code.getDefaultMessage())
                : code.getDefaultMessage();

        String traceId = LogUtils.getTraceId();
        ErrorResponse body = ErrorResponse.of(
                (ErrorCode) code, // 단일 enum만 쓸 때 캐스팅. 여러 enum을 쓸 경우 ErrorResponse.of(BaseErrorCode …) 오버로드로 바꿔도 됨
                safeMessage,
                req.getRequestURI(),
                traceId,
                fields
        );
        return ResponseEntity.status(code.getStatus()).body(body);
    }

    /* 1) 도메인 예외 */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Object> handleAppException(AppException ex, HttpServletRequest req) {
        return build(ex.getErrorCode(), ex.getMessage(), req, null, ex);
    }

    /* 2) @Valid DTO 검증 실패 (Body/Form) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fields = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fields.add(ErrorResponse.FieldError.of(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
        );
        return build(ErrorCode.INVALID_INPUT_VALUE, "입력값 검증에 실패했습니다.", req, fields, ex);
    }

    /* 3) 바인딩/쿼리 파라미터 검증 실패 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBind(BindException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fields = new ArrayList<>();
        ex.getFieldErrors().forEach(fe ->
                fields.add(ErrorResponse.FieldError.of(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
        );
        return build(ErrorCode.INVALID_INPUT_VALUE, "요청 파라미터 바인딩 실패", req, fields, ex);
    }

    /* 4) Path/Query validation (@Validated @PathVariable/@RequestParam) */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        List<ErrorResponse.FieldError> fields = ex.getConstraintViolations().stream()
                .map(v -> ErrorResponse.FieldError.of(v.getPropertyPath().toString(), v.getMessage(), v.getInvalidValue()))
                .toList();
        return build(ErrorCode.INVALID_INPUT_VALUE, "입력값 검증에 실패했습니다.", req, fields, ex);
    }

    /* 5) 타입 불일치, 필수 파라미터/경로 변수 누락 */
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingPathVariableException.class
    })
    public ResponseEntity<Object> handleBadParams(Exception ex, HttpServletRequest req) {
        return build(ErrorCode.TYPE_MISMATCH, ex.getMessage(), req, null, ex);
    }

    /* 6) JSON 파싱/변환 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleJsonParse(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(ErrorCode.JSON_PARSE_ERROR, "JSON 파싱 오류: " + ex.getMostSpecificCause().getMessage(), req, null, ex);
    }
    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<Object> handleConversion(HttpMessageConversionException ex, HttpServletRequest req) {
        return build(ErrorCode.INVALID_INPUT_VALUE, "메시지 변환 오류: " + ex.getMostSpecificCause().getMessage(), req, null, ex);
    }

    /* 7) HTTP 레벨 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleMethod(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(ErrorCode.METHOD_NOT_ALLOWED, ex.getMessage(), req, null, ex);
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return build(ErrorCode.INVALID_INPUT_VALUE, ex.getMessage(), req, null, ex);
    }
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Object> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpServletRequest req) {
        return build(ErrorCode.INVALID_INPUT_VALUE, ex.getMessage(), req, null, ex);
    }
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNotFound(NoHandlerFoundException ex, HttpServletRequest req) {
        return build(ErrorCode.RESOURCE_NOT_FOUND, "경로를 찾을 수 없습니다: " + ex.getRequestURL(), req, null, ex);
    }

    /* 8) DB/영속성 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return build(ErrorCode.DB_ERROR, "무결성 위반: " + ex.getMostSpecificCause().getMessage(), req, null, ex);
    }

    /* 9) 최후 보루 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAny(Exception ex, HttpServletRequest req) {
        return build(ErrorCode.INTERNAL_ERROR, ex.getMessage(), req, null, ex);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleBusiness(BusinessException ex) {
        BaseErrorCode ec = ex.getErrorCode();
        return ResponseEntity.status(ec.getStatus())
                .body(CommonApiResponse.error(ec, ex.getMessage())); // ← 새 오버로드 사용
    }
}
