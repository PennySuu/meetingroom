# 会议室预约平台（MVP）

前后端分离；契约以 `openspec/specs/openapi.yaml` 为准，工程约束见 `openspec/config.yaml`。

## 环境要求

| 组件 | 版本 |
|------|------|
| JDK | **17+**（Spring Boot 3 必需；请勿使用 JDK 8） |
| Node.js | **20.19+** |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Redis | 7.0+ |

## 本地启动（开发）

1. 启动依赖（任选其一）  
   - `docker compose up -d` 启动本仓库中的 MySQL 与 Redis  
   - 或自行安装并创建库 `meetingroom`、用户与密码与 `backend/src/main/resources/application-dev.yml` 中默认值一致（可用环境变量覆盖）。

2. 后端（`SPRING_PROFILES_ACTIVE=dev`）：  
   - 进入 `backend/` 执行 `mvn spring-boot:run`  
   - Flyway 会在启动时迁移数据库；默认监听 `http://localhost:8080`。

3. 前端：  
   - 进入 `frontend/` 执行 `npm install` 与 `npm run dev`  
   - 默认 `http://localhost:5173`，通过 `frontend/.env.development` 中 `VITE_API_BASE` 指向后端。

4. CORS：后端默认允许 `http://localhost:5173`，可通过环境变量 `MEETINGROOM_CORS_ORIGINS` 追加（逗号分隔）。

## 构建

- 后端：`cd backend && mvn -DskipTests package`
- 前端：`cd frontend && npm run build`

## 文档

业务与交互见 `doc/`；API 说明见 `openspec/specs/README.md` 与 Swagger UI（开发环境：`/swagger-ui.html`）。
