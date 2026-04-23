package com.meetingroom.room.dto;

import java.util.List;

public record RoomDto(Long id, String name, String location, int capacity, List<String> amenities) {
}
