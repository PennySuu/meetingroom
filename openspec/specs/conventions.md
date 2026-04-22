# 契约约定（补充 OpenAPI）

## 1. 业务时间区间（预约）

- 语义采用 **左闭右开**：**`[startAt, endAt)`**。  
- 判定与重叠：见后端实现中的 **`start < end_i AND end > start_i`**（同为半开区间时相邻无重叠）。
- **`endAt > startAt`**；单次时长 **`endAt - startAt ≤ 4 小时`**；**不允许跨自然日**（在 **`startAt`、`endAt`** 换算到同一 **`timeZone`** 后，`localDate(startAt)` 必须等于 **`localDate(endAt)`**）。

### 日历查询日（`GET .../calendar`）

- **`date`**：日历 **业务日**（`YYYY-MM-DD`），解释在 **`timeZone`**（默认 **`Asia/Shanghai`**）之下。
- 服务端返回该业务日内与 **`[startAt, endAt)`** 相交的所有 **ACTIVE** 占用片段；条目 **`startAt`/`endAt`** 使用 **ISO-8601**（建议带偏移或 `Z`，与数据库/领域层一致）。

### 「过去 / 现在 / 可取消」

- **唯一权威**：服务端当前时间（可配置时钟 skew，如 ±30 秒）。
- **可取消**：仅当 **`startAt > now()`**（未开始）。

## 2. 分页与排序

与 `openapi.yaml` 及 `openspec/config.yaml` 一致：

- Query：**`page`**（从 **0** 起）、**`size`**（默认 **10**）。
- 响应：**`page`、`size`、`total`、`items`**。
- UI 若展示「第 1 页」，视为 **`page = 0`** 的呈现层映射。

## 3. 筛选参数格式（会议室列表）

- **`capacityGte`**：整数，最小容纳人数下限（含）。
- **`amenity`**：**同一键重复**（`style=form`, `explode=true`），例：`amenity=projector&amenity=whiteboard`。  
  **不得**使用未约定的逗号拼接形式，除非将来版本在 OpenAPI 中显式新增。

## 4. CSRF

采用 Cookie 会话时，变更类请求须满足 **`openspec/config.yaml`** 中的 CSRF / SameSite 约定；具体 Token 名称、请求头与登录后下发方式由实现与 **`openapi.yaml`** 中 **Global** 说明共同定义（若团队为双端分离，须固定 **`X-CSRF-Token`** 或等价头并写入 OpenAPI **components.parameters**）。

## 5. 枚举与错误码

业务错误码以 **`openapi.yaml`** **`ErrorCode`** 枚举及 **`shared.security` / error_codes** 为准；HTTP 映射见 **`openspec/config.yaml`**。
