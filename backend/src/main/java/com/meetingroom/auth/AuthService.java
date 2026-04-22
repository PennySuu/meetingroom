package com.meetingroom.auth;

import com.meetingroom.auth.domain.AppUser;
import com.meetingroom.auth.dto.LoginRequest;
import com.meetingroom.auth.dto.RegisterRequest;
import com.meetingroom.auth.dto.UserSnapshotDto;
import com.meetingroom.common.exception.BusinessException;
import com.meetingroom.common.exception.ErrorCodes;
import com.meetingroom.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter loginRateLimiter;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, LoginRateLimiter loginRateLimiter) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.loginRateLimiter = loginRateLimiter;
    }

    @Transactional
    public UserSnapshotDto register(RegisterRequest req, HttpServletRequest request) {
        if (userMapper.countByUsername(req.username()) > 0) {
            throw new BusinessException(ErrorCodes.USER_USERNAME_CONFLICT, "用户名已被占用", HttpStatus.CONFLICT);
        }
        AppUser user = new AppUser();
        user.setUsername(req.username());
        user.setPasswordHash(passwordEncoder.encode(req.passwordPrehash()));
        user.setDisplayName(req.displayName());
        userMapper.insert(user);
        establishSession(request, user);
        return toSnapshot(user);
    }

    public UserSnapshotDto login(LoginRequest req, HttpServletRequest request) {
        if (!loginRateLimiter.allow(request)) {
            throw new BusinessException(ErrorCodes.RATE_LIMIT_EXCEEDED, "访问过于频繁，请稍后再试", HttpStatus.TOO_MANY_REQUESTS);
        }
        AppUser user = userMapper.findByUsername(req.username());
        if (user == null || !passwordEncoder.matches(req.passwordPrehash(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCodes.AUTH_UNAUTHORIZED, "登录失败，请检查用户名或密码", HttpStatus.UNAUTHORIZED);
        }
        establishSession(request, user);
        return toSnapshot(user);
    }

    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private void establishSession(HttpServletRequest request, AppUser user) {
        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), user.getDisplayName());
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }

    private static UserSnapshotDto toSnapshot(AppUser user) {
        return new UserSnapshotDto(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
