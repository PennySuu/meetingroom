## ADDED Requirements

### Requirement: 口令参数与注册登录登出契约

系统 SHALL 实现 `GET /v1/auth/password-params`、`POST /v1/auth/register`、`POST /v1/auth/login`、`POST /v1/auth/logout`，请求与响应字段 SHALL 与 `openspec/specs/openapi.yaml` 一致；注册与登录请求体 SHALL **仅包含 `passwordPrehash`**（不得传输明文密码字段）。

#### Scenario: 注册新用户成功

- **WHEN** 客户端按 formulaVersion=1 计算 `passwordPrehash` 并调用注册接口且用户名唯一
- **THEN** 返回成功信封且后续可使用同凭证登录

#### Scenario: 登录失败防枚举

- **WHEN** 用户名或密码错误导致登录失败
- **THEN** 对外错误文案 SHALL 不明确区分「用户不存在」与「密码错误」（与产品设计一致）

### Requirement: 服务端口令存储与会话

系统 SHALL 对 `passwordPrehash` 使用 BCrypt 或 Argon2 存储；SHALL 将会话存于 Redis 并通过 **HTTPOnly Cookie** 下发会话凭证；日志 SHALL 不包含明文口令或 `passwordPrehash`。

#### Scenario: 已登录访问需鉴权接口

- **WHEN** 用户已登录并携带有效会话 Cookie 调用需鉴权接口
- **THEN** 请求被允许且返回业务数据信封

### Requirement: 登录限流

系统 SHALL 对 `POST /v1/auth/login` 实施严于普通只读接口的限流策略；触发限流时 SHALL 返回 HTTP 429 且错误码为 `RATE_LIMIT_EXCEEDED`，并 SHOULD 返回 `Retry-After`（与 OpenAPI 一致）。

#### Scenario: 短时间多次登录失败触发限流

- **WHEN** 同一来源在短时间内超过阈值调用登录接口
- **THEN** 响应为 429 且客户端可根据 `Retry-After` 冷却
