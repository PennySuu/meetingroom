package com.meetingroom.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateBookingRequest(
        @NotNull Long roomId,
        @NotNull Instant startAt,
        @NotNull Instant endAt,
        @NotBlank @Size(max = 200) String title
) {
}
