package com.meetingroom.auth;

import com.meetingroom.auth.domain.AppUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO app_user (username, password_hash, display_name) VALUES (#{username}, #{passwordHash}, #{displayName})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AppUser user);

    @Select(
            "SELECT id, username, password_hash AS passwordHash, display_name AS displayName "
                    + "FROM app_user WHERE username = #{username}")
    AppUser findByUsername(String username);

    @Select(
            "SELECT id, username, password_hash AS passwordHash, display_name AS displayName "
                    + "FROM app_user WHERE id = #{id}")
    AppUser findById(Long id);

    @Select("SELECT COUNT(*) FROM app_user WHERE username = #{username}")
    long countByUsername(String username);

    @Select("SELECT id FROM app_user WHERE id = #{id} FOR UPDATE")
    Long lockById(Long id);
}
