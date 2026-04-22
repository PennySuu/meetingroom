package com.meetingroom.booking;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IdempotencyMapper {

    @Select(
            "SELECT booking_id FROM booking_idempotency WHERE idempotency_key = #{key} AND user_id = #{userId}")
    Long findBookingId(@Param("key") String key, @Param("userId") Long userId);

    @Insert(
            "INSERT INTO booking_idempotency (idempotency_key, user_id, booking_id) "
                    + "VALUES (#{key}, #{userId}, #{bookingId})")
    void insert(@Param("key") String key, @Param("userId") Long userId, @Param("bookingId") Long bookingId);
}
