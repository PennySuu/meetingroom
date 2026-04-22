/** P1：工作日栅格 08:00–20:00，30 分钟粒度（业务日按 Asia/Shanghai 字符串拼接） */

export const GRID_START_MIN = 8 * 60
export const GRID_END_MIN = 20 * 60
export const SLOT_MINUTES = 30

/** 从意图中的 ISO 区间还原栅格 inclusive 索引（与本页选时规则一致） */
export function isoRangeToSlotIndexes(startIso: string, endIso: string): {
  start: number
  end: number
} {
  const sm = new Date(startIso).getHours() * 60 + new Date(startIso).getMinutes()
  const em = new Date(endIso).getHours() * 60 + new Date(endIso).getMinutes()
  const start = Math.round((sm - GRID_START_MIN) / SLOT_MINUTES)
  const end = Math.round((em - SLOT_MINUTES - GRID_START_MIN) / SLOT_MINUTES)
  return { start, end }
}

export function slotCount(): number {
  return (GRID_END_MIN - GRID_START_MIN) / SLOT_MINUTES
}

/** 业务日 + 当日分钟数（0–24h）→ ISO 字符串 +08:00 */
export function buildShanghaiIso(dateYmd: string, minutesFromMidnight: number): string {
  const h = Math.floor(minutesFromMidnight / 60)
  const mi = minutesFromMidnight % 60
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${dateYmd}T${pad(h)}:${pad(mi)}:00+08:00`
}

export function slotIndexToMinutesFromMidnight(slotIndex: number): number {
  return GRID_START_MIN + slotIndex * SLOT_MINUTES
}

/** 将 ISO 时刻映射到当日相对于 0 点的分钟数（用于与栅格对齐，按本地解析） */
export function isoToMinutesFromMidnight(iso: string): number {
  const d = new Date(iso)
  return d.getHours() * 60 + d.getMinutes()
}

/** 在时间轴容器内绘制占用块（相对 08:00–20:00 业务日） */
export function occupiedStyle(
  dateYmd: string,
  startIso: string,
  endIso: string,
): { top: string; height: string } {
  const gridStart = new Date(`${dateYmd}T08:00:00+08:00`).getTime()
  const gridEnd = new Date(`${dateYmd}T20:00:00+08:00`).getTime()
  const dur = gridEnd - gridStart
  const s = new Date(startIso).getTime()
  const e = new Date(endIso).getTime()
  const topPct = Math.max(0, ((s - gridStart) / dur) * 100)
  const botPct = Math.min(100, ((e - gridStart) / dur) * 100)
  const h = Math.max(0, botPct - topPct)
  return { top: `${topPct}%`, height: `${h}%` }
}
