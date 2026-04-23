package com.meetingroom.room;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoomMapper {

    @Select(
            "SELECT id, name, location, capacity, amenities AS amenitiesJson "
                    + "FROM meeting_room WHERE id = #{id}")
    RoomRow findById(Long id);

    @Select(
            "SELECT id, name, location, capacity, amenities AS amenitiesJson FROM meeting_room "
                    + "WHERE (#{capacityGte} IS NULL OR capacity >= #{capacityGte}) ORDER BY id")
    List<RoomRow> findAll(@Param("capacityGte") Integer capacityGte);

    /** 事务内锁定会议室行，串行化同一会议室的预约写入 */
    @Select("SELECT id FROM meeting_room WHERE id = #{id} FOR UPDATE")
    Long lockById(Long id);
}
