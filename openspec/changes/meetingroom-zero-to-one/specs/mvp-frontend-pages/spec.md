## ADDED Requirements

### Requirement: 路由与访客策略 A

前端 SHALL 提供会议室列表与预约日历路由且 **未登录可访问**；SHALL 提供「我的预约」路由且 **未登录 SHALL 重定向至登录并携带 redirect**；SHALL 在用户点击「确认预约」提交时若未登录则引导登录/注册并保留预约意图（与产品设计一致）。

#### Scenario: 未登录访问我的预约

- **WHEN** 访客直接打开「我的预约」URL
- **THEN** 应用跳转登录页且可在登录后回到目标页

#### Scenario: 访客提交预约前需登录

- **WHEN** 访客在日历页填写时段与主题并提交
- **THEN** 在未登录情况下无法完成创建预约 API 调用；登录成功后应能恢复上下文并继续流程

### Requirement: 会议室列表与日历页体验

会议室列表页 SHALL 展示名称、位置、容量、设施，并提供容量与设施筛选；日历页 SHALL 展示会议室上下文（名称与位置），已占用格 SHALL 仅展示「已占用」类文案而不得展示他人主题。

#### Scenario: 日历占用展示隐私

- **WHEN** 日历渲染他人占用时段
- **THEN** UI 不显示他人会议主题文本

### Requirement: 我的预约与取消交互

「我的预约」页 SHALL 支持会议室与日期范围筛选与分页；SHALL 对未开始预约提供取消按钮并进行二次确认；对已进行中或已结束预约 SHALL 禁用或隐藏取消并展示说明。

#### Scenario: 取消未开始预约需确认

- **WHEN** 用户点击取消未开始的预约
- **THEN** 出现确认对话框且确认后调用取消接口并更新列表

### Requirement: UI 技术约束

前端 SHALL **不依赖 Element UI / Element Plus**；SHALL 使用 Headless 组件（如 Radix Vue）与 Tailwind + Design Token 实现界面（与前端评审一致）。

#### Scenario: 依赖审计

- **WHEN** 检查 `frontend/package.json` 依赖树
- **THEN** 不包含 `@element-plus/*` 或与评审冲突的 Element 体系依赖
