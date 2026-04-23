package com.meetingroom.booking;

import com.meetingroom.common.exception.BusinessException;
import com.meetingroom.common.exception.ErrorCodes;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * MySQL GET_LOCK：保障同一幂等键串行。
 */
@Component
public class MysqlNamedLock {

    private static final int LOCK_TIMEOUT_SEC = 20;

    private final JdbcTemplate jdbcTemplate;

    public MysqlNamedLock(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void execute(String lockName, Runnable action) {
        Integer acquired = jdbcTemplate.queryForObject(
                "SELECT GET_LOCK(?, ?)", Integer.class, lockName, LOCK_TIMEOUT_SEC);
        if (acquired == null || acquired != 1) {
            throw new BusinessException(ErrorCodes.OPERATION_FAILED, "系统繁忙，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            action.run();
        } finally {
            jdbcTemplate.queryForObject("SELECT RELEASE_LOCK(?)", Integer.class, lockName);
        }
    }
}
