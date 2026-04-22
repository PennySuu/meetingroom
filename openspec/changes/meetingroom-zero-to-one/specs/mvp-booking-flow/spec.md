## ADDED Requirements

### Requirement: 创建预约与幂等

系统 SHALL 实现 `POST /v1/bookings`，请求体包含 `roomId`、`startAt`、`endAt`、`title`；客户端 SHALL 在请求头携带 **`Idempotency-Key`**；服务端 SHALL 在约定 TTL 内对相同键返回相同业务结果以避免重复创建。

#### Scenario: 重复提交携带相同幂等键

- **WHEN** 客户端因网络抖动使用相同 `Idempotency-Key` 重试创建同一预约
- **THEN** 不会产生两条有效预约记录

### Requirement: 业务校验与冲突错误

系统 SHALL 拒绝开始时间早于当前服务器时间、拒绝时长超过 4 小时、拒绝跨自然日的区间（与产品设计一致）；SHALL 检测会议室时段重叠并返回 **`BOOKING_CONFLICT_ROOM`**；SHALL 检测用户同时段占用其他会议室并返回 **`BOOKING_CONFLICT_USER`**，且 **`data.conflictingRoom` SHALL 包含 `id` 与 `name`**。

#### Scenario: 会议室已被占用

- **WHEN** 新预约时间与同一会议室已有有效预约重叠
- **THEN** 响应为 HTTP 409 且 `code` 为 `BOOKING_CONFLICT_ROOM`

#### Scenario: 用户同时段已约其他会议室

- **WHEN** 新预约与用户已有预约时间重叠且会议室不同
- **THEN** 响应为 HTTP 409 且 `code` 为 `BOOKING_CONFLICT_USER`，且 `data.conflictingRoom.name` 可用于展示

### Requirement: 我的预约与取消

系统 SHALL 实现 `GET /v1/bookings/mine` 支持 `roomId`、`dateFrom`、`dateTo` 与分页；SHALL 实现 `POST /v1/bookings/{bookingId}/cancel`，且仅允许 **预约所属用户** 取消 **尚未开始** 的有效预约。

#### Scenario: 取消未开始的本人预约

- **WHEN** 当前时间早于预约开始时间且调用者为预约用户
- **THEN** 预约变为已取消（或等价状态）且时段释放

#### Scenario: 拒绝取消进行中或已结束预约

- **WHEN** 当前时间已达到或超过开始时间，或预约已结束
- **THEN** 取消请求失败并返回约定业务错误码（非静默成功）

### Requirement: 并发下预约一致性

系统 SHALL 在并发两个请求抢占同一空档时，至多一个成功创建，另一个 SHALL 收到冲突类错误且不产生重复有效预约。

#### Scenario: 并发抢占同一空档

- **WHEN** 两个并发请求提交重叠时间段且会议室相同
- **THEN** 仅一条预约记录进入有效状态
