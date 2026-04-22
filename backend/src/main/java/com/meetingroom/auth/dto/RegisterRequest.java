package com.meetingroom.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 64) String username,
        @NotBlank @Pattern(regexp = "^[0-9a-f]{64}$") String passwordPrehash,
        @Size(max = 64) String displayName
) {
}
