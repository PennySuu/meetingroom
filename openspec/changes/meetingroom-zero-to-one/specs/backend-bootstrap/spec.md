## ADDED Requirements

### Requirement: 后端工程具备生产级骨架与双环境配置

系统 SHALL 提供位于 `backend/` 的 Maven 工程，使用 Java 17、Spring Boot 3.3.x，根 Java 包名为 **`com.meetingroom`**（禁止使用 `example` 作为包名片段）。系统 SHALL 提供 **`dev` 与 `prod` 两套 Spring Profile**，分别对应本地开发与生产部署的配置外置方式（数据源、Redis、会话密钥等不得硬编码于源码）。

#### Scenario: 使用 dev Profile 启动应用

- **WHEN** 开发者以 `dev` Profile 启动 Spring Boot 应用且 MySQL/Redis 可达
- **THEN** 应用成功监听配置端口并通过 Actuator `/health`（或等价）返回 UP，且日志输出当前激活 Profile 为 `dev`

#### Scenario: 使用 prod Profile 启动应用

- **WHEN** 运维以 `prod` Profile 启动应用并注入必需环境变量或外部配置
- **THEN** 应用启动成功且敏感配置不从仓库明文读取

### Requirement: 本地开发启动自动执行数据库结构迁移

系统 SHALL 在 **`dev` Profile** 下于应用启动流程中 **自动执行 Flyway（或团队书面批准的等价迁移工具）**，以创建或演进数据库表结构；迁移脚本纳入版本控制并可在干净库上从零创建 schema。

#### Scenario: 空数据库首次启动

- **WHEN** 指向空 schema 的数据库首次以 `dev` 启动应用
- **THEN** Flyway 执行迁移后业务表存在且应用进入就绪状态

### Requirement: API 横切能力与分层约束

系统 SHALL 实现统一响应信封（`success`、`code`、`message`、`data`）、`X-Request-ID` 透传与错误码到 HTTP 状态的映射；Controller 层 SHALL 不包含复杂业务规则；持久化 SHALL 使用 MyBatis 且 SQL 使用参数绑定。

#### Scenario: 非法请求返回统一错误体

- **WHEN** 客户端调用任意 `/v1/**` 接口且请求体验证失败
- **THEN** 响应体符合信封格式且 HTTP 状态码与 `openspec/config.yaml` 约定一致（如 422 对应校验类错误码）
