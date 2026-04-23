package com.meetingroom.common.web;

import com.meetingroom.common.api.Envelope;
import com.meetingroom.common.exception.BusinessException;
import com.meetingroom.common.exception.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Envelope<Object>> handleBusiness(BusinessException ex, HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("BusinessException: {} {}", ex.getCode(), ex.getMessage());
        }
        HttpHeaders headers = new HttpHeaders();
        if (ErrorCodes.RATE_LIMIT_EXCEEDED.equals(ex.getCode())) {
            headers.add("Retry-After", "60");
        }
        return ResponseEntity.status(ex.getStatus()).headers(headers)
                .body(Envelope.fail(ex.getCode(), ex.getMessage(), ex.getData()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Envelope<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("参数校验失败");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Envelope.fail(ErrorCodes.VALIDATION_FAILED, msg, null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Envelope<Object>> handleConstraint(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Envelope.fail(ErrorCodes.VALIDATION_FAILED, ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Envelope<Object>> handleOther(Exception ex) {
        log.error("Unhandled error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Envelope.fail(ErrorCodes.OPERATION_FAILED, "服务暂时不可用，请稍后重试", null));
    }
}
