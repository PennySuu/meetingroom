package com.meetingroom.booking;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.Instant;
import java.util.List;

@Mapper
public interface BookingMapper {

    @Select(
            "SELECT start_at AS startAt, end_at AS endAt FROM booking WHERE room_id = #{roomId} "
                    + "AND status = 'ACTIVE' AND start_at < #{dayEnd} AND end_at > #{dayStart}")
    List<BookingSegmentRow> findActiveOccupiedSegments(
            @Param("roomId") Long roomId,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd);

    @Select(
            "SELECT COUNT(*) FROM booking WHERE room_id = #{roomId} AND status = 'ACTIVE' "
                    + "AND start_at < #{endAt} AND end_at > #{startAt}")
    long countRoomOverlap(
            @Param("roomId") Long roomId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt);

    @Select(
            "SELECT b.room_id AS roomId, mr.name AS roomName FROM booking b "
                    + "JOIN meeting_room mr ON mr.id = b.room_id WHERE b.user_id = #{userId} "
                    + "AND b.room_id <> #{excludeRoomId} AND b.status = 'ACTIVE' "
                    + "AND b.start_at < #{endAt} AND b.end_at > #{startAt} LIMIT 1")
    UserConflictRow findUserOverlapOtherRoom(
            @Param("userId") Long userId,
            @Param("excludeRoomId") Long excludeRoomId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt);

    @Insert(
            "INSERT INTO booking (room_id, user_id, title, start_at, end_at, status, version) "
                    + "VALUES (#{roomId}, #{userId}, #{title}, #{startAt}, #{endAt}, #{status}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(BookingRow row);

    @Select(
            "SELECT id, room_id AS roomId, user_id AS userId, title, start_at AS startAt, "
                    + "end_at AS endAt, status FROM booking WHERE id = #{id}")
    BookingRow findById(Long id);

    @Update(
            "UPDATE booking SET status = 'CANCELLED', version = version + 1, "
                    + "updated_at = CURRENT_TIMESTAMP(3) WHERE id = #{id} AND user_id = #{userId} "
                    + "AND status = 'ACTIVE' AND start_at > #{now}")
    int cancelMine(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("now") Instant now);

    long countMine(
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            @Param("rangeStart") Instant rangeStart,
            @Param("rangeEnd") Instant rangeEnd);

    List<BookingRow> findMinePage(
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            @Param("rangeStart") Instant rangeStart,
            @Param("rangeEnd") Instant rangeEnd,
            @Param("offset") int offset,
            @Param("limit") int limit);
}
