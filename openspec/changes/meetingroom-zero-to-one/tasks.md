## 1. 仓库基线、双环境与本地依赖（后端 + 前端 + 测试）

- [x] 1.1 **后端**：创建 `backend/` Maven 工程（Java 17、Spring Boot 3.3.x、MyBatis、Flyway、Redis、SpringDoc），根包 **`com.meetingroom`**；配置 **`dev`/`prod` Profile**、外部化 `application-dev/prod`；`dev` 启动 **自动 Flyway 迁移**；提供 Actuator 健康检查与全局 **API 信封**、`X-Request-ID`、`ControllerAdvice` 错误码映射（对齐 `openspec/config.yaml`）。**前端**：创建 `frontend/`（Vue3+Vite+TS strict、ESLint），配置 **`development`/`production`** 与 `import.meta.env`；Axios 单例（`withCredentials`、`X-Request-ID`）、目录骨架（`views`/`api`/`stores`/`composables`）。**测试**：后端 Spring 上下文或最小 Web 层测试验证应用可启动且 Flyway 于测试库可运行；Vitest 示例测试通过；根目录可选 `docker-compose.yml`（MySQL+Redis）并在 README 文档化启动顺序。

## 2. 用户认证模块（后端 + 前端 + 测试）

- [x] 2.1 **后端**：实现 `GET /v1/auth/password-params`、`POST .../register`、`POST .../login`、`POST .../logout`；`passwordPrehash` 服务端哈希存储（BCrypt/Argon2）；Redis Session + HTTPOnly Cookie；登录 **限流**（429、`RATE_LIMIT_EXCEEDED`，对齐 OpenAPI）；CSRF/CORS 策略与 `dev` 本地联调配置文档化。**前端**：注册/登录页、调用 password-params 与 **`usePasswordHasher`（formulaVersion=1）**；Axios 拦截器处理 401/`AUTH_*`；登录失败统一文案。**测试**：JUnit（注册登录 Service/Controller 集成测试或 Testcontainers）；Vitest（哈希与拦截器逻辑）；**E2E**：登录成功路径 smoke（Playwright 或 Cypress 其一，脚本入库）。

## 3. 会议室与日历（只读 API + 访客页面）（后端 + 前端 + 测试）

- [x] 3.1 **后端**：`meeting_room` 表与 Flyway 种子数据；`GET /v1/rooms`（`capacityGte`、重复 `amenity`）、`GET /v1/rooms/{id}`、`GET .../calendar`（必填 `date`，响应项仅 `startAt`/`endAt`）；访客可访问。**前端**：会议室列表页（筛选、空状态）、日历页（P1：工作日栅格 + 占用条，「已占用」文案）；路由 `/rooms`、`/rooms/:roomId/calendar`。**测试**：MyBatis Mapper/Service 集成测试或 `@Sql` 数据集；Vitest（日历布局/时间工具）；API 契约抽检（可选 Spring REST Docs 或 MockMvc + OpenAPI 字段断言）。

## 4. 预约创建与幂等（后端 + 前端 + 测试）

- [x] 4.1 **后端**：`booking` 表；`POST /v1/bookings` 含 **`Idempotency-Key`** 幂等存储；校验过去时间、≤4h、不跨日；双重冲突（`BOOKING_CONFLICT_ROOM` / `BOOKING_CONFLICT_USER` 且 **`data.conflictingRoom`**）；事务 + 锁顺序防并发双插。**前端**：预约表单（侧栏/弹窗）、生成并复用幂等键、409 冲突提示（含用户冲突会议室名）、成功后刷新日历。**测试**：JUnit 并发/事务测试或锁集成测试；Vitest（表单校验与错误映射）；E2E：访客登录后创建预约成功路径。

## 5. 我的预约与取消（后端 + 前端 + 测试）

- [x] 5.1 **后端**：`GET /v1/bookings/mine`（筛选分页排序）；`POST .../cancel`（仅本人且未开始）；错误码与 OpenAPI 一致。**前端**：`/bookings/mine` 路由守卫；筛选、分页、取消二次确认；进行中/已结束禁用取消。**测试**：后端取消边界单元/集成测试；Vitest（按钮禁用规则）；E2E：取消未开始预约并校验日历释放（或接口校验）。

## 6. 访客策略 A 与意图恢复（以前端为主，配套后端联调与测试）

- [x] 6.1 **后端**：确认公开只读与受保护路径鉴权无误。**前端**：未登录可浏览列表/日历；提交预约前跳转登录并 **sessionStorage/Pinia 恢复 roomId、时段、标题**；401 全局处理与 redirect。**测试**：Vitest（意图序列化）；E2E：访客填单→登录→继续预约完整路径。

## 7. 生产化加固与文档（后端 + 前端 + 测试）

- [x] 7.1 **后端**：日志 JSON/结构化（按环境）、限流接入点（Resilience4j/Sentinel）、Micrometer/Prometheus 可选暴露；优雅停机；敏感配置禁止入库。**前端**：生产构建分包与环境变量审查；Sentry 可选占位。**测试**：覆盖率基线达成（对齐 config：整体 ≥70%，核心模块提高）；CI 脚本（Maven test + npm test + 可选 E2E）；更新 `doc/` 部署与环境说明、本变更摘要。

## 8. SDD 契约门禁（跨职能）

- [x] 8.1 实现全程以 **`openspec/specs/openapi.yaml`** 为准；若行为变更 **先更新 OpenAPI 与 `doc/`** 再改代码；变更目录 `openspec/changes/meetingroom-zero-to-one/specs/**` 与实现对齐并完成走查。
