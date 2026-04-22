## Context

本项目从零建立会议室预约 MVP：业务与体验以 `doc/` 为准，API 契约以 `openspec/specs/openapi.yaml` 为单一事实来源；工程与安全约束以 `openspec/config.yaml` 为准。当前仓库缺少 `backend/` 与 `frontend/` 可运行代码，需在 **生产级基线**（双环境、迁移、测试、可观测性接入点）下交付完整 MVP。

## Goals / Non-Goals

**Goals:**

- 交付可本地与生产部署的前后端工程，**Java 根包 `com.meetingroom`**（禁止 `example`），前后端各至少 **开发/生产** 两套配置。
- **本地 `dev` 启动即执行 Flyway 迁移**，自动创建/演进表结构；预置会议室种子数据可在迁移或独立脚本中完成（与评审一致）。
- 实现 OpenAPI 所列认证、会议室、预约端点；统一响应信封、错误码、分页与鉴权；会话使用 **HTTPOnly Cookie + Redis**；写操作满足 **CSRF 策略与 `Idempotency-Key`**（按契约）。
- 前端实现访客策略 A、列表/日历/我的预约/登录注册流程；**禁止 Element Plus**；UI 采用 **Headless + Tailwind + Design Token**。
- **每个开发任务**同时覆盖后端实现、前端实现与自动化测试（单元/集成/组件 + 关键 E2E 路径）。

**Non-Goals:**

- 会议室后台管理、改期、周期性会议、消息通知、组织权限、多租户（见产品设计延后项）。
- 自助找回密码（产品 MVP 可文案引导管理员）。
- 完整监控大屏与全套 SRE 告警（仅预留 Micrometer/健康检查等接入点）。

## Decisions

| 决策 | 内容 | 理由与备选方案 |
|------|------|----------------|
| 契约来源 | 以 **`openapi.yaml`** 为行为准绳；与产品冲突时先改契约再实现 | SDD；备选「口头约定」不可审计 |
| 后端框架 | Spring Boot 3.3.x + Java 17 + MyBatis | 与 `openspec/config.yaml` 一致；备选 JPA 会增加与现有评审差异 |
| 数据库迁移 | **Flyway**，`dev` profile **启动即迁移** | 满足「本地启动自动初始化表结构」；备选 Liquibase 需团队统一 |
| 根包名 | **`com.meetingroom`** | 用户强制要求禁止 `example` |
| 会话 | Redis 存储 Session，Cookie **HTTPOnly、Secure（生产）、SameSite** | 对齐 config 与后端评审；备选纯 JWT 需额外 XSS 防护 |
| 并发 | 预约创建在事务内 **重叠检测 + 悲观锁顺序（先 room 后 user）**，可选死锁重试 | 与后端评审一致；备选纯乐观锁对「插入冲突」较弱 |
| 口令 | 客户端 **`passwordPrehash`**（参见 `openspec/specs/README.md` formulaVersion=1），服务端 BCrypt/Argon2 | 契约已定 |
| 前端 UI | **Radix Vue（或等价 Headless）+ Tailwind + CSS 变量 Token** | 前端评审已定；禁止 Element Plus |
| 日历交互 | **P1**：固定时段栅格（如工作日 08:00–20:00，30/60 分钟粒度），契约仍为 ISO 区间 | 控制 MVP 复杂度；P2 再增强拖拽 |
| 环境维度 | 后端 **`dev`/`prod`** Spring Profiles；前端 **`development`/`production`** | 用户明确要求至少两套环境 |

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 前后端分端口导致 Cookie/CSRF/CORS 配置错误 | 文档化同源代理或明确跨域、`withCredentials`、SameSite 组合；提供本地示例配置 |
| 日历「简化栅格」与任意 ISO 区间接口混用导致边界 bug | 前端提交前做强校验；后端 **`VALIDATION_FAILED`** 兜底 |
| 热点会议室日历查询压垮 DB | 短 TTL Redis 缓存 + 索引（`room_id`+时间）；必要时限流 |
| Flyway 与手工改库冲突 | 禁止生产手工改结构；迁移脚本审查与 CI |
| 任务并行开发导致接口漂移 | API-first；契约变更走 PR + openapi 更新 |

## Migration Plan

1. **开发环境**：开发者启动 Docker Compose（或自建）MySQL + Redis → 配置 `backend` `application-dev` → 启动后端触发 Flyway → 启动前端 `development` 指向本地 API。
2. **数据**：首版迁移创建 `user`、`meeting_room`、`booking` 等表；种子数据插入会议室；不在迁移中放真实用户密码。
3. **生产**：使用 `prod` Profile 与密钥注入；HTTPS 终止；数据库由运维或云 RDS；回滚策略为「保留 DB、回滚应用镜像 + 必要时前向兼容迁移」。
4. **契约**：发布前核对 `openapi.yaml` 版本与部署说明。

## Open Questions

- 生产是否采用 **同域反向代理**（推荐）以避免第三方 Cookie 问题；若跨域，具体 CORS 与 CSRF 头组合需部署时拍板。
- E2E 选用 Playwright 还是 Cypress（择一并写入 `frontend/package.json` 脚本）。
- Redis 是否在一阶段即启用 **日历缓存**（可先读 DB、接口层留扩展点）。
