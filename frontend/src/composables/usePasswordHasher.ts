/**
 * formulaVersion = 1：passwordPrehash = SHA-256(pepper + ':' + plainPassword) 的小写十六进制。
 * @see openspec/specs/README.md
 */
export async function computePasswordPrehash(pepper: string, plainPassword: string): Promise<string> {
  const input = `${pepper}:${plainPassword}`
  const bytes = new TextEncoder().encode(input)
  const digest = await crypto.subtle.digest('SHA-256', bytes)
  const arr = Array.from(new Uint8Array(digest))
  return arr.map((b) => b.toString(16).padStart(2, '0')).join('')
}
