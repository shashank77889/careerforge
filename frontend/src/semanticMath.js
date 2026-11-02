export const MODEL = 'Xenova/all-MiniLM-L6-v2'
export const THRESHOLD = 0.55
export function cosine(a, b) {
  if (!a.length || a.length !== b.length) return 0
  let dot = 0, aa = 0, bb = 0
  for (let i = 0; i < a.length; i++) { dot += a[i] * b[i]; aa += a[i] ** 2; bb += b[i] ** 2 }
  return aa && bb ? Math.max(-1, Math.min(1, dot / Math.sqrt(aa * bb))) : 0
}
export function lexical(a, b) {
  const tokens = text => new Set(text.toLowerCase().match(/[a-z0-9]+/g) || [])
  const x = tokens(a), y = tokens(b)
  return x.size ? [...x].filter(t => y.has(t)).length / x.size : 0
}
export function metrics(rows, key, threshold) {
  let tp = 0, fp = 0, tn = 0, fn = 0
  for (const row of rows) {
    const predicted = row[key] >= threshold
    if (predicted && row.expected) tp++
    else if (predicted) fp++
    else if (row.expected) fn++
    else tn++
  }
  const precision = tp + fp ? tp / (tp + fp) : 0
  const recall = tp + fn ? tp / (tp + fn) : 0
  return { tp, fp, tn, fn, precision, recall, f1: precision + recall ? 2 * precision * recall / (precision + recall) : 0, accuracy: rows.length ? (tp + tn) / rows.length : 0 }
}
