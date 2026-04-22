package com.meetingroom.booking;

import com.meetingroom.auth.UserMapper;
import com.meetingroom.booking.dto.BookingDto;
import com.meetingroom.booking.dto.CreateBookingRequest;
import com.meetingroom.booking.dto.PageBooking;
import com.meetingroom.common.exception.BusinessException;
import com.meetingroom.common.exception.ErrorCodes;
import com.meetingroom.config.MeetingRoomProperties;
import com.meetingroom.room.RoomMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingService {

    private static final Duration MAX_BOOKING_DURATION = Duration.ofHours(4);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    private final BookingMapper bookingMapper;
    private final IdempotencyMapper idempotencyMapper;
    private final RoomMapper roomMapper;
    private final UserMapper userMapper;
    private final MysqlNamedLock mysqlNamedLock;
    private final MeetingRoomProperties meetingRoomProperties;

    public BookingService(
            BookingMapper bookingMapper,
            IdempotencyMapper idempotencyMapper,
            RoomMapper roomMapper,
            UserMapper userMapper,
            MysqlNamedLock mysqlNamedLock,
            MeetingRoomProperties meetingRoomProperties) {
        this.bookingMapper = bookingMapper;
        this.idempotencyMapper = idempotencyMapper;
        this.roomMapper = roomMapper;
        this.userMapper = userMapper;
        this.mysqlNamedLock = mysqlNamedLock;
        this.meetingRoomProperties = meetingRoomProperties;
    }

    /**
     * 幂等锁 + 事务：在同一请求线程内开启事务，覆盖锁内全部数据库操作。
     */
    @Transactional
    public BookingDto create(Long userId, UUID idempotencyKey, CreateBookingRequest req) {
        String lockName = buildIdempotencyLockName(userId, idempotencyKey);
        BookingDto[] holder = new BookingDto[1];
        mysqlNamedLock.execute(lockName, () -> holder[0] = createBody(userId, idempotencyKey.toString(), req));
        return holder[0];
    }

    private BookingDto createBody(Long userId, String idempotencyKey, CreateBookingRequest req) {
        Long existingId = idempotencyMapper.findBookingId(idempotencyKey, userId);
        if (existingId != null) {
            return requireBookingDto(existingId);
        }

        validateBookingTimes(req.startAt(), req.endAt());

        if (roomMapper.findById(req.roomId()) == null) {
            throw new BusinessException(ErrorCodes.ROOM_NOT_FOUND, "会议室不存在", HttpStatus.NOT_FOUND);
        }

        roomMapper.lockById(req.roomId());
        userMapper.lockById(userId);

        if (bookingMapper.countRoomOverlap(req.roomId(), req.startAt(), req.endAt()) > 0) {
            throw new BusinessException(ErrorCodes.BOOKING_CONFLICT_ROOM, "该时段会议室已被占用", HttpStatus.CONFLICT,
                    Map.of());
        }

        UserConflictRow userConflict =
                bookingMapper.findUserOverlapOtherRoom(userId, req.roomId(), req.startAt(), req.endAt());
        if (userConflict != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("conflictingRoom", Map.of(
                    "id", userConflict.getRoomId(),
                    "name", userConflict.getRoomName()));
            throw new BusinessException(ErrorCodes.BOOKING_CONFLICT_USER, "该时段您已预约其他会议室", HttpStatus.CONFLICT,
                    data);
        }

        BookingRow row = new BookingRow();
        row.setRoomId(req.roomId());
        row.setUserId(userId);
        row.setTitle(req.title());
        row.setStartAt(req.startAt());
        row.setEndAt(req.endAt());
        row.setStatus("ACTIVE");
        bookingMapper.insert(row);

        try {
            idempotencyMapper.insert(idempotencyKey, userId, row.getId());
        } catch (DuplicateKeyException e) {
            Long bid = idempotencyMapper.findBookingId(idempotencyKey, userId);
            if (bid != null) {
                return requireBookingDto(bid);
            }
            throw e;
        }

        return toDto(bookingMapper.findById(row.getId()));
    }

    @Transactional(readOnly = true)
    public PageBooking listMine(
            Long userId,
            Long roomId,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size) {
        if ((dateFrom == null) != (dateTo == null)) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED, "请同时提供开始日期与结束日期", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Instant rangeStart = null;
        Instant rangeEnd = null;
        if (dateFrom != null) {
            rangeStart = dateFrom.atStartOfDay(BUSINESS_ZONE).toInstant();
            rangeEnd = dateTo.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
            if (rangeEnd.isBefore(rangeStart)) {
                throw new BusinessException(ErrorCodes.VALIDATION_FAILED, "日期范围无效", HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        long total = bookingMapper.countMine(userId, roomId, rangeStart, rangeEnd);
        int offset = page * size;
        List<BookingRow> rows =
                bookingMapper.findMinePage(userId, roomId, rangeStart, rangeEnd, offset, size);
        List<BookingDto> items = rows.stream().map(this::toDto).toList();
        return new PageBooking(page, size, total, items);
    }

    @Transactional
    public BookingDto cancel(Long userId, Long bookingId) {
        Instant now = Instant.now();
        BookingRow row = bookingMapper.findById(bookingId);
        if (row == null) {
            throw new BusinessException(ErrorCodes.BOOKING_NOT_FOUND, "预约不存在", HttpStatus.NOT_FOUND);
        }
        if (!row.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCodes.PERMISSION_DENIED, "无权操作该预约", HttpStatus.FORBIDDEN);
        }
        if (!"ACTIVE".equals(row.getStatus())) {
            throw new BusinessException(ErrorCodes.BOOKING_CANNOT_CANCEL, "该预约无法取消", HttpStatus.CONFLICT);
        }
        if (!row.getStartAt().isAfter(now)) {
            throw new BusinessException(ErrorCodes.BOOKING_CANNOT_CANCEL, "会议已开始或已结束，无法取消", HttpStatus.CONFLICT);
        }
        int updated = bookingMapper.cancelMine(bookingId, userId, now);
        if (updated == 0) {
            throw new BusinessException(ErrorCodes.BOOKING_CANNOT_CANCEL, "该预约无法取消", HttpStatus.CONFLICT);
        }
        return toDto(bookingMapper.findById(bookingId));
    }

    private BookingDto requireBookingDto(Long id) {
        BookingRow row = bookingMapper.findById(id);
        if (row == null) {
            throw new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, "资源不存在", HttpStatus.NOT_FOUND);
        }
        return toDto(row);
    }

    private void validateBookingTimes(Instant startAt, Instant endAt) {
        if (!endAt.isAfter(startAt)) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED, "结束时间必须晚于开始时间", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Duration d = Duration.between(startAt, endAt);
        if (d.compareTo(MAX_BOOKING_DURATION) > 0) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED, "单次预约最长 4 小时", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        LocalDate ds = startAt.atZone(BUSINESS_ZONE).toLocalDate();
        LocalDate de = endAt.atZone(BUSINESS_ZONE).toLocalDate();
        if (!ds.equals(de)) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED, "单次预约不能跨自然日", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Instant now = Instant.now();
        long skew = meetingRoomProperties.getClockSkewSeconds();
        if (startAt.isBefore(now.minusSeconds(skew))) {
            throw new BusinessException(ErrorCodes.VALIDATION_FAILED, "开始时间不能早于当前时间", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private BookingDto toDto(BookingRow row) {
        return new BookingDto(
                row.getId(),
                row.getRoomId(),
                row.getTitle(),
                row.getStartAt(),
                row.getEndAt(),
                row.getStatus());
    }

    private static String buildIdempotencyLockName(Long userId, UUID key) {
        String raw = "idem_" + userId + "_" + key;
        if (raw.length() <= 64) {
            return raw;
        }
        return raw.substring(0, 64);
    }
}
