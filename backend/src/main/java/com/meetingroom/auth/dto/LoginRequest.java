package com.meetingroom.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank String username,
        @NotBlank @Pattern(regexp = "^[0-9a-f]{64}$") String passwordPrehash
) {
}
