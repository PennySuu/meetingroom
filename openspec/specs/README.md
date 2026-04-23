# SDD：会议室预约平台 · API 契约

本文档目录为 **API-first / 契约先行** 的单一事实来源（Single Source of Truth）。实现代码与 `doc/` 技术评审若有出入，以本目录 **`openapi.yaml`** 为准并同步修订其它文档。

## 文件说明

| 文件 | 说明 |
|------|------|
| `openapi.yaml` | MVP 全部 REST 端点、请求/响应 Schema、公开错误载荷、访客可读字段边界 |
| `conventions.md` | 时间与区间语义、分页与筛选约定、与环境无关的契约补充 |

## 与其它工件的关系

- **`openspec/config.yaml`**：项目全局规范（技术栈、安全总则、分页 envelope 形状等）。
- **`doc/`**：面向评审的阅读材料；引用本目录条目作为落地依据。
- **变更流程**：契约变更应先更新 **`openapi.yaml`**（及必要的 `conventions.md`），再更新实现与 PR 描述。

## 密码传输（与本目录 OpenAPI 一致）

1. 客户端调用 **`GET /v1/auth/password-params`**（可缓存数分钟），取得 `pepper` 与 **`formulaVersion`**。
2. **formulaVersion = 1**（当前唯一版本）时：`passwordPrehash = 小写十六进制字符串`，其值为对 UTF-8 字节序列执行 SHA-256 的消息摘要，输入字符串为 **`pepper + ":" + plainPassword`**（三部分均为常规字符串拼接，`plainPassword` 为用户原始口令）。
3. **`POST /v1/auth/register`** / **`POST /v1/auth/login`** 的请求体仅传输 **`passwordPrehash`**（64 位十六进制字符），不设明文密码字段。
4. 服务端对 **`passwordPrehash`** 字符串使用 BCrypt（或 Argon2）存入 **`user.password_hash`**。**日志与审计中不得输出用户原始口令**，并不得将 **`passwordPrehash`** 写入日志或异常详情（对齐 `openspec/config.yaml` 脱敏要求）。

传输层仍必须使用 **HTTPS**。

## 访客可读数据边界

日历占用（`GET /v1/rooms/{roomId}/calendar`）每条占用仅包含 **`startAt`、`endAt`**（ISO-8601）。**不包含**他人会议 **`title`、`userId`、显示名**。

## 冲突错误（409）

若业务码为 **`BOOKING_CONFLICT_USER`**，`data` 必须包含 **`conflictingRoom`**（至少含 **`id`、`name`**），以便产品文案展示「您已在某会议室占用该时段」。详见 **`openapi.yaml`** 中的 `BookingConflictUserData`。

## 速率限制（登录）

**`POST /v1/auth/login`** 须在实现上采用 **严于普通读接口** 的限流（推荐默认：**同一 IP + 可选同一用户名**，例如每分钟 ≤ 5 次）；响应 **429**、`code: RATE_LIMIT_EXCEEDED`，并返回 **`Retry-After`**（秒）。

---

*语言与目录约定遵循 `openspec/config.yaml`。*
