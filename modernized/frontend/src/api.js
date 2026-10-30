const TOKEN_KEY = 'carddemo.token'

export function getSession() {
  const raw = sessionStorage.getItem('carddemo.session')
  return raw ? JSON.parse(raw) : null
}
export function setSession(s) {
  sessionStorage.setItem('carddemo.session', JSON.stringify(s))
  if (s?.token) sessionStorage.setItem(TOKEN_KEY, s.token)
}
export function clearSession() {
  sessionStorage.removeItem('carddemo.session')
  sessionStorage.removeItem(TOKEN_KEY)
}
export function getToken() { return sessionStorage.getItem(TOKEN_KEY) }
export function isAdmin() { return getSession()?.userType === 'A' }

/** Applies the regular-user theme (grey background, green buttons) to <body>.
 *  Admin ('A') sessions and signed-out state keep the default terminal theme. */
export function applyRoleTheme() {
  document.body.classList.toggle('theme-user', getSession()?.userType === 'U')
}

export class ApiError extends Error {
  constructor(message, status) { super(message); this.status = status }
}

async function req(method, path, body) {
  const res = await fetch(path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(getToken() ? { 'X-Auth-Token': getToken() } : {}),
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new ApiError(data.message || `Request failed (${res.status})`, res.status)
  return data
}

export const api = {
  get: (p) => req('GET', p),
  post: (p, b) => req('POST', p, b),
  put: (p, b) => req('PUT', p, b),
  del: (p) => req('DELETE', p),
}
