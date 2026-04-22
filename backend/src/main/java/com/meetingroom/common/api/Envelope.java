package com.meetingroom.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一 API 信封，对齐 openspec/config.yaml 与 openapi.yaml。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Envelope<T>(
        boolean success,
        String code,
        String message,
        T data
) {
    public static <T> Envelope<T> ok(String message, T data) {
        return new Envelope<>(true, "SUCCESS", message, data);
    }

    public static Envelope<Void> okEmpty(String message) {
        return new Envelope<>(true, "SUCCESS", message, null);
    }

    public static <T> Envelope<T> fail(String code, String message, T data) {
        return new Envelope<>(false, code, message, data);
    }
}
