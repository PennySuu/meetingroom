package com.meetingroom.common.exception;

/**
 * 业务错误码（与 openapi ErrorCode 枚举对齐）。
 */
public final class ErrorCodes {

    private ErrorCodes() {
    }

    public static final String SUCCESS = "SUCCESS";
    public static final String AUTH_UNAUTHORIZED = "AUTH_UNAUTHORIZED";
    public static final String AUTH_TOKEN_EXPIRED = "AUTH_TOKEN_EXPIRED";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String USER_USERNAME_CONFLICT = "USER_USERNAME_CONFLICT";
    public static final String ROOM_NOT_FOUND = "ROOM_NOT_FOUND";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String BOOKING_NOT_FOUND = "BOOKING_NOT_FOUND";
    public static final String BOOKING_CONFLICT_ROOM = "BOOKING_CONFLICT_ROOM";
    public static final String BOOKING_CONFLICT_USER = "BOOKING_CONFLICT_USER";
    public static final String BOOKING_CANNOT_CANCEL = "BOOKING_CANNOT_CANCEL";
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String PERMISSION_DENIED = "PERMISSION_DENIED";
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String OPERATION_FAILED = "OPERATION_FAILED";
}
