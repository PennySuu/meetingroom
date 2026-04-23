package com.meetingroom.booking;

import java.time.Instant;

public class BookingSegmentRow {

    private Instant startAt;
    private Instant endAt;

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
    }
}
