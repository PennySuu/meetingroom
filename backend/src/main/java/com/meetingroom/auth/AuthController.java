package com.meetingroom.auth;

import com.meetingroom.auth.dto.LoginRequest;
import com.meetingroom.auth.dto.PasswordParamsDto;
import com.meetingroom.auth.dto.RegisterRequest;
import com.meetingroom.auth.dto.UserSnapshotDto;
import com.meetingroom.common.api.Envelope;
import com.meetingroom.config.MeetingRoomProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final MeetingRoomProperties meetingRoomProperties;

    public AuthController(AuthService authService, MeetingRoomProperties meetingRoomProperties) {
        this.authService = authService;
        this.meetingRoomProperties = meetingRoomProperties;
    }

    @GetMapping("/password-params")
    public Envelope<PasswordParamsDto> passwordParams() {
        PasswordParamsDto data = new PasswordParamsDto(meetingRoomProperties.getAuth().getPepper(), 1);
        return Envelope.ok("OK", data);
    }

    @PostMapping("/register")
    public ResponseEntity<Envelope<UserSnapshotDto>> register(
            @Valid @RequestBody RegisterRequest body,
            HttpServletRequest request) {
        UserSnapshotDto user = authService.register(body, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Envelope.ok("注册成功", user));
    }

    @PostMapping("/login")
    public Envelope<UserSnapshotDto> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        UserSnapshotDto user = authService.login(body, request);
        return Envelope.ok("登录成功", user);
    }

    @PostMapping("/logout")
    public Envelope<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return Envelope.okEmpty("已退出登录");
    }
}
