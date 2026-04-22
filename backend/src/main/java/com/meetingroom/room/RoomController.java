package com.meetingroom.room;

import com.meetingroom.common.api.Envelope;
import com.meetingroom.room.dto.OccupiedSlotsData;
import com.meetingroom.room.dto.RoomDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public Envelope<List<RoomDto>> list(
            @RequestParam(required = false) Integer capacityGte,
            @RequestParam(required = false) List<String> amenity) {
        List<RoomDto> data = roomService.listRooms(capacityGte, amenity);
        return Envelope.ok("OK", data);
    }

    @GetMapping("/{roomId}")
    public Envelope<RoomDto> get(@PathVariable Long roomId) {
        return Envelope.ok("OK", roomService.getRoom(roomId));
    }

    @GetMapping("/{roomId}/calendar")
    public Envelope<OccupiedSlotsData> calendar(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String timeZone) {
        return Envelope.ok("OK", roomService.getCalendar(roomId, date, timeZone));
    }
}
