package com.meetingroom.security;

import com.meetingroom.common.exception.BusinessException;
import com.meetingroom.common.exception.ErrorCodes;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long requireUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessException(ErrorCodes.AUTH_UNAUTHORIZED, "请先登录", HttpStatus.UNAUTHORIZED);
        }
        return principal.getId();
    }
}
