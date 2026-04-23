## Why

会议室预约平台当前仅有需求、产品与前后端评审文档以及 `openspec/specs/openapi.yaml` 契约，缺少可按 SDD 落地的 **生产级前后端工程** 与 **端到端可验收的 MVP 业务能力**。本变更目标是从零交付「工程初始化 + MVP 预约全流程」，使团队在统一契约与规范下持续演进，而非停留在演示级骨架。

## What Changes

- **新增** `backend/`：Java 17 + Spring Boot 3.3.x + Maven + MyBatis；基础包名 **`com.meetingroom`**（**禁止使用 `example` 包名**）；分层架构（Controller → Service → Repository）；统一响应信封、`X-Request-ID`、错误码映射；**`dev` / `prod` 两套 Spring Profile**；敏感配置外置；本地 **`dev` 启动时通过 Flyway（或等价）自动执行数据库迁移** 初始化/演进表结构；集成 MySQL、Redis（会话与可选短缓存）、SpringDoc/OpenAPI；生产化基线（健康检查、优雅停机、日志与限流接入点等按 `openspec/config.yaml` 取舍落地）。
- **新增** `frontend/`：Vue 3 + TypeScript（`strict`）+ Vite + ESLint；**`development` / `production` 两套构建与 `import.meta.env`**；Vue Router + Pinia + Axios（`withCredentials`、拦截器）；目录与质量基线对齐 `openspec/config.yaml` 与前端评审（**禁止 Element Plus**；Headless + Tailwind + Design Token）。
- **MVP 业务能力**（行为以 **`openspec/specs/openapi.yaml`** 为准，与 `doc/` 一致）：用户注册/登录/登出（密码 `passwordPrehash` 契约）；会议室列表/详情/日历（访客可读；日历仅暴露占用时间，不暴露他人主题）；创建预约（`Idempotency-Key`、双重冲突、≤4 小时、不约过去、不跨自然日）；我的预约（筛选分页）与取消（仅未开始）；登录限流与 429/`Retry-After` 等行为与契约一致。
- **测试**：每个实现任务包含 **后端**（JUnit/MyBatis 测试或 WebMvc/集成测试）、**前端**（Vitest + 组件/组合式函数）、**关键路径 E2E 预留/样例**（Playwright 或 Cypress），覆盖率目标遵循 `openspec/config.yaml`。
- **契约维护**：实现中若发现与 OpenAPI 不一致，**先修订 `openspec/specs/openapi.yaml` 并同步 `doc/`**，再改代码（SDD）。

## Capabilities

### New Capabilities

- `backend-bootstrap`：后端工程、双环境 Profile、Flyway 自动迁移、横切 concern、MyBatis、Redis、容器/本地依赖说明。
- `frontend-bootstrap`：前端工程、双环境、Router/Pinia/Axios 基线、ESLint、Vitest、目录约定与 UI 基线（Headless + Tailwind）。
- `mvp-user-auth`：password-params、注册/登录/登出、会话（HTTPOnly Cookie + Redis Session）、预哈希口令存储、登录限流与防枚举。
- `mvp-room-calendar`：会议室预置数据、列表筛选、日历按日查询、访客可读与隐私字段约束。
- `mvp-booking-flow`：创建预约（幂等）、双重冲突检测与事务/锁策略、我的预约与取消、业务校验与错误码。
- `mvp-frontend-pages`：路由与访客策略 A、会议室列表与日历页、我的预约、登录回跳与预约意图恢复、错误与 409 用户体验（含 `BOOKING_CONFLICT_USER.data.conflictingRoom`）。

### Modified Capabilities

- （无）全局 API 契约以存量 **`openspec/specs/openapi.yaml`** 为权威；本次以变更内 `specs/*/spec.md` 描述 **增量需求与验收**，不与 OpenAPI 冲突；若需行为变更则走 OpenAPI 修订流程。

## Impact

- **新增目录**：`backend/`、`frontend/`（及测试、迁移脚本、环境示例等）。
- **基础设施**：开发者需本地或容器内提供 **MySQL 8.0+**、**Redis 7.0+**；可选 Docker Compose 统一拉起。
- **依赖与运维**：后续部署需配置 HTTPS、密钥与限流阈值；监控与告警按 `openspec/config.yaml` 渐进启用。
- **文档**：实现与契约变更需同步 `doc/` 与变更说明，保持可审计。
