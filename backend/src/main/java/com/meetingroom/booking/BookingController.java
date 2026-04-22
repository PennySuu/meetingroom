package com.meetingroom.booking;

import com.meetingroom.booking.dto.BookingDto;
import com.meetingroom.booking.dto.CreateBookingRequest;
import com.meetingroom.booking.dto.PageBooking;
import com.meetingroom.common.api.Envelope;
import com.meetingroom.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Envelope<BookingDto>> create(
            @RequestHeader("Idempotency-Key") UUID idempotencyKey,
            @Valid @RequestBody CreateBookingRequest body) {
        Long userId = SecurityUtils.requireUserId();
        BookingDto dto = bookingService.create(userId, idempotencyKey, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Envelope.ok("预约成功", dto));
    }

    @GetMapping("/mine")
    public Envelope<PageBooking> mine(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.requireUserId();
        PageBooking data = bookingService.listMine(userId, roomId, dateFrom, dateTo, page, size);
        return Envelope.ok("OK", data);
    }

    @PostMapping("/{bookingId}/cancel")
    public Envelope<BookingDto> cancel(@PathVariable Long bookingId) {
        Long userId = SecurityUtils.requireUserId();
        BookingDto dto = bookingService.cancel(userId, bookingId);
        return Envelope.ok("已取消预约", dto);
    }
}
