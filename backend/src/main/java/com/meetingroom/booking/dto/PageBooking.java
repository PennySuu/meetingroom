package com.meetingroom.booking.dto;

import java.util.List;

public record PageBooking(int page, int size, long total, List<BookingDto> items) {
}
