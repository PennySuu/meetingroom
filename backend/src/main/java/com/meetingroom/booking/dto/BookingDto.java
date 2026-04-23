package com.meetingroom.booking.dto;

import java.time.Instant;

public record BookingDto(
        Long id,
        Long roomId,
        String title,
        Instant startAt,
        Instant endAt,
        String status
) {
}
