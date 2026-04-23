## ADDED Requirements

### Requirement: 前端工程具备生产级骨架与双环境构建

系统 SHALL 提供位于 `frontend/` 的 Vite + Vue 3 + TypeScript 工程，Node 版本符合 `openspec/config.yaml`（≥20.19），开启 **`strict` TypeScript**。系统 SHALL 提供 **`development` 与 `production` 两套模式**，通过 `import.meta.env` 区分 API 基址等变量，且生产构建产物可静态托管。

#### Scenario: 开发模式启动

- **WHEN** 开发者运行开发服务器脚本
- **THEN** 应用可访问且环境为 `development`，Axios 使用开发环境基址配置

#### Scenario: 生产构建

- **WHEN** 执行生产构建命令
- **THEN** 生成优化后的产物且无 TypeScript 编译错误（在约定范围内）

### Requirement: HTTP 客户端与目录约定

系统 SHALL 使用 Axios 单例，默认 **`withCredentials: true`**；请求拦截器 SHALL 注入 `X-Request-ID`；响应拦截器 SHALL 解析统一信封。源码目录 SHALL 符合 `openspec/config.yaml`（如 `src/views`、`src/api`、`src/stores`、`src/composables` 等）。

#### Scenario: 调用需要 Cookie 的接口

- **WHEN** 前端请求后端需会话的接口
- **THEN** 浏览器随请求携带 Cookie 且开发环境 CORS 配置允许凭证

### Requirement: 质量基线与测试脚手架

系统 SHALL 配置 ESLint；SHALL 集成 Vitest 与 `@vue/test-utils`；SHALL 提供可运行的示例测试（例如工具函数或占位组件测试）以证明流水线可执行。

#### Scenario: 运行单元测试

- **WHEN** 执行前端测试命令
- **THEN** 测试进程退出码为 0
