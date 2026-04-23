package com.meetingroom.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final Object data;

    public BusinessException(String code, String message, HttpStatus status) {
        this(code, message, status, null);
    }

    public BusinessException(String code, String message, HttpStatus status, Object data) {
        super(message);
        this.code = code;
        this.status = status;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }
}
