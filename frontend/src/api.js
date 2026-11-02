export const API_BASE = (import.meta.env.PROD ? '/api' : (import.meta.env.VITE_API_URL || '/api')).replace(/\/$/, '')

export function clearSession() {
  for (const key of ['token', 'userId', 'userName', 'userEmail']) localStorage.removeItem(key)
}

export async function api(path, options = {}) {
  const token = localStorage.getItem('token')
  let response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.body && !(options.body instanceof FormData) ? { 'Content-Type': 'application/json' } : {}),
        ...options.headers,
      },
      signal: options.signal ? AbortSignal.any([options.signal, AbortSignal.timeout(20000)]) : AbortSignal.timeout(20000),
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new Error('Unable to reach CareerForge. Check your connection and try again.', { cause: error })
  }
  const text = await response.text()
  let data
  try { data = text ? JSON.parse(text) : null } catch { data = text }
  if (response.status === 401 && !path.startsWith('/users/')) {
    clearSession()
    window.dispatchEvent(new Event('session-expired'))
    throw new Error('Your session expired. Please sign in again.')
  }
  if (!response.ok) {
    const fallback = response.status >= 500 ? 'CareerForge is temporarily unavailable. Keep your input and try again in a moment.' : response.status === 403 ? 'You do not have access to this account’s data.' : response.status === 404 ? 'This item could not be found. Refresh and try again.' : 'Unable to save this change. Check the fields and try again.'
    throw new Error(data?.error || data?.message || fallback)
  }
  return data
}

export const userPath = (resource) => `/${resource}/user/${localStorage.getItem('userId')}`
export const userKey = (key) => `careerforge:${localStorage.getItem('userId')}:${key}`
export function readSaved(key, fallback) {
  try { return JSON.parse(localStorage.getItem(userKey(key))) ?? fallback } catch { return fallback }
}



