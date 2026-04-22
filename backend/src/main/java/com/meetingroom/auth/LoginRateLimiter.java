package com.meetingroom.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录接口按 IP 滑动窗口限流（默认每分钟 5 次），对齐 OpenAPI 要求。
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_PER_WINDOW = 5;
    private static final long WINDOW_MILLIS = 60_000L;

    private final ConcurrentHashMap<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public boolean allow(HttpServletRequest request) {
        String ip = resolveClientIp(request);
        long now = System.currentTimeMillis();
        Deque<Long> dq = buckets.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (dq) {
            while (!dq.isEmpty() && now - dq.peekFirst() > WINDOW_MILLIS) {
                dq.pollFirst();
            }
            if (dq.size() >= MAX_PER_WINDOW) {
                return false;
            }
            dq.addLast(now);
        }
        return true;
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
