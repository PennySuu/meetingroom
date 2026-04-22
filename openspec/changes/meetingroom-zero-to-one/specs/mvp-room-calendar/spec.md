## ADDED Requirements

### Requirement: 会议室列表与筛选（访客可读）

系统 SHALL 实现 `GET /v1/rooms` 与 `GET /v1/rooms/{roomId}`，支持 `capacityGte` 与重复 query key 形式的 `amenity` 多选；未登录用户 SHALL 可调用上述只读接口。

#### Scenario: 按容量与设施筛选列表

- **WHEN** 客户端传入 `capacityGte` 与多个 `amenity` 参数
- **THEN** 返回的会议室集合满足筛选条件且响应分页/列表结构与 OpenAPI 一致

### Requirement: 日历查询与隐私

系统 SHALL 实现 `GET /v1/rooms/{roomId}/calendar` 且 **必填** `date=YYYY-MM-DD`；响应中每条占用记录 SHALL **仅包含** `startAt` 与 `endAt`（或契约允许字段），SHALL **不得**包含他人会议主题或用户标识。

#### Scenario: 访客查询某日占用

- **WHEN** 未登录用户请求某会议室某日日历
- **THEN** 返回成功且 `items` 中不包含他人 `title` 或 `userId`

### Requirement: 会议室数据预置

系统 SHALL 通过迁移或种子数据预置至少一间会议室记录；应用 SHALL 不提供「在线创建会议室」的公开 API（与需求一致）。

#### Scenario: 首次安装后有会议室数据

- **WHEN** 在完成迁移与种子的全新环境调用会议室列表
- **THEN** 列表非空（除非数据脚本被刻意关闭且环境变量明确用于空库演示）
