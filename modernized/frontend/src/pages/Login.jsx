import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, setSession } from '../api'
import { Screen, Field } from '../ui'

/** COSGN00C — Signon screen. */
export default function Login() {
  const nav = useNavigate()
  const [userId, setUserId] = useState('')
  const [password, setPassword] = useState('')
  const [err, setErr] = useState('')

  async function submit(e) {
    e.preventDefault()
    setErr('')
    try {
      const s = await api.post('/api/auth/login', { userId, password })
      setSession(s)
      nav(s.userType === 'A' ? '/admin/menu' : '/menu')
    } catch (ex) {
      setErr(ex.message)
    }
  }

  return (
    <Screen tranId="CC00" program="COSGN00C" title="Signon Screen">
      <div className="center-title">Welcome to CardDemo</div>
      <form onSubmit={submit}>
        <Field label="User ID">
          <input value={userId} onChange={e => setUserId(e.target.value)} maxLength={8} autoFocus />
        </Field>
        <Field label="Password">
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} maxLength={8} />
        </Field>
        <div style={{ margin: '18px 0 0 250px' }}>
          <button type="submit">ENTER - Sign On</button>
        </div>
      </form>
      <div className="dim" style={{ marginTop: 16, fontSize: 12, color: '#6e7681' }}>
        Regular user: USER0001 / PASSWORD &nbsp;•&nbsp; Admin: ADMIN001 / PASSWORD
      </div>
      {err && <div className="msg err">{err}</div>}
    </Screen>
  )
}
