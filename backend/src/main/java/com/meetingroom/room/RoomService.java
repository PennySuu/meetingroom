package com.meetingroom.room;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetingroom.booking.BookingMapper;
import com.meetingroom.common.exception.BusinessException;
import com.meetingroom.common.exception.ErrorCodes;
import com.meetingroom.room.dto.OccupiedSlotDto;
import com.meetingroom.room.dto.OccupiedSlotsData;
import com.meetingroom.room.dto.RoomDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoomService {

    private final RoomMapper roomMapper;
    private final BookingMapper bookingMapper;
    private final ObjectMapper objectMapper;

    public RoomService(RoomMapper roomMapper, BookingMapper bookingMapper, ObjectMapper objectMapper) {
        this.roomMapper = roomMapper;
        this.bookingMapper = bookingMapper;
        this.objectMapper = objectMapper;
    }

    public List<RoomDto> listRooms(Integer capacityGte, List<String> amenitiesFilter) {
        List<RoomRow> rows = roomMapper.findAll(capacityGte);
        List<RoomDto> result = new ArrayList<>();
        Set<String> required = amenitiesFilter == null ? Set.of() : new HashSet<>(amenitiesFilter);
        for (RoomRow row : rows) {
            List<String> amenityList = parseAmenities(row.getAmenitiesJson());
            if (!required.isEmpty() && !amenityList.containsAll(required)) {
                continue;
            }
            result.add(new RoomDto(row.getId(), row.getName(), row.getLocation(), row.getCapacity(), amenityList));
        }
        return result;
    }

    public RoomDto getRoom(Long roomId) {
        RoomRow row = roomMapper.findById(roomId);
        if (row == null) {
            throw new BusinessException(ErrorCodes.ROOM_NOT_FOUND, "会议室不存在", HttpStatus.NOT_FOUND);
        }
        return new RoomDto(row.getId(), row.getName(), row.getLocation(), row.getCapacity(),
                parseAmenities(row.getAmenitiesJson()));
    }

    public OccupiedSlotsData getCalendar(Long roomId, LocalDate date, String timeZoneId) {
        if (roomMapper.findById(roomId) == null) {
            throw new BusinessException(ErrorCodes.ROOM_NOT_FOUND, "会议室不存在", HttpStatus.NOT_FOUND);
        }
        ZoneId zone = ZoneId.of(timeZoneId == null || timeZoneId.isBlank() ? "Asia/Shanghai" : timeZoneId);
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant();

        var segments = bookingMapper.findActiveOccupiedSegments(roomId, dayStart, dayEnd);
        List<OccupiedSlotDto> items = new ArrayList<>();
        for (var seg : segments) {
            Instant s = seg.getStartAt().isBefore(dayStart) ? dayStart : seg.getStartAt();
            Instant e = seg.getEndAt().isAfter(dayEnd) ? dayEnd : seg.getEndAt();
            if (s.isBefore(e)) {
                items.add(new OccupiedSlotDto(s, e));
            }
        }
        return new OccupiedSlotsData(items);
    }

    private List<String> parseAmenities(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            return List.of();
        }
    }
}
