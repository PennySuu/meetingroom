package com.meetingroom.room.dto;

import java.time.Instant;

public record OccupiedSlotDto(Instant startAt, Instant endAt) {
}
