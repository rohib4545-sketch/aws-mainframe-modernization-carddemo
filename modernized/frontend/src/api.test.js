import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { api, ApiError, getSession, setSession, clearSession, getToken, isAdmin } from './api'

const SESSION = { token: 'tok-123', userId: 'USER0001', userType: 'U', firstName: 'TEST' }

beforeEach(() => sessionStorage.clear())
afterEach(() => vi.unstubAllGlobals())

describe('session helpers', () => {
  it('round-trips a session through sessionStorage', () => {
    setSession(SESSION)
    expect(getSession()).toEqual(SESSION)
    expect(getToken()).toBe('tok-123')
  })

  it('clears the session and token', () => {
    setSession(SESSION)
    clearSession()
    expect(getSession()).toBeNull()
    expect(getToken()).toBeNull()
  })

  it('returns null when no session exists', () => {
    expect(getSession()).toBeNull()
    expect(getToken()).toBeNull()
    expect(isAdmin()).toBe(false)
  })

  it('isAdmin only for userType A', () => {
    setSession({ ...SESSION, userType: 'A' })
    expect(isAdmin()).toBe(true)
    setSession({ ...SESSION, userType: 'U' })
    expect(isAdmin()).toBe(false)
  })
})

describe('api client', () => {
  function mockFetch(status, data) {
    const spy = vi.fn(async () => ({
      ok: status >= 200 && status < 300,
      status,
      json: async () => data,
    }))
    vi.stubGlobal('fetch', spy)
    return spy
  }

  it('sends JSON body and auth token header', async () => {
    setSession(SESSION)
    const spy = mockFetch(200, { ok: true })
    await api.post('/api/auth/login', { userId: 'U', password: 'P' })
    const [path, opts] = spy.mock.calls[0]
    expect(path).toBe('/api/auth/login')
    expect(opts.method).toBe('POST')
    expect(opts.headers['X-Auth-Token']).toBe('tok-123')
    expect(JSON.parse(opts.body)).toEqual({ userId: 'U', password: 'P' })
  })

  it('omits the token header when signed out', async () => {
    const spy = mockFetch(200, {})
    await api.get('/api/menus')
    expect(spy.mock.calls[0][1].headers['X-Auth-Token']).toBeUndefined()
  })

  it('throws ApiError carrying status and server message', async () => {
    mockFetch(401, { message: 'Wrong Password. Try again ...' })
    const err = await api.post('/api/auth/login', {}).catch(e => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(401)
    expect(err.message).toContain('Wrong Password')
  })

  it('falls back to a generic message when the body is not JSON', async () => {
    const spy = vi.fn(async () => ({
      ok: false,
      status: 500,
      json: async () => { throw new Error('not json') },
    }))
    vi.stubGlobal('fetch', spy)
    const err = await api.get('/api/broken').catch(e => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.message).toBe('Request failed (500)')
  })
})
