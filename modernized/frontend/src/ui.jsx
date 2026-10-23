import React from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { getSession, clearSession, api } from './api'

export function now() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return {
    date: `${p(d.getMonth() + 1)}/${p(d.getDate())}/${String(d.getFullYear()).slice(2)}`,
    time: `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`,
  }
}

/** 3270-style screen frame: header rows + error/message line + footer. */
export function Screen({ tranId, program, title, children, message, error, onBack }) {
  const nav = useNavigate()
  const t = now()
  const s = getSession()
  return (
    <div className="screen">
      <div className="hdr">
        <span className="dim">Tran: {tranId}</span>
        <span className="t1">{title || 'CardDemo Application'}</span>
        <span className="dim">{t.date}</span>
      </div>
      <div className="hdr">
        <span className="dim">Prog: {program}</span>
        <span className="t2">{s ? `Signed on as ${s.userId}` : 'AWS CardDemo'}</span>
        <span className="dim">{t.time}</span>
      </div>
      <div className="body">{children}</div>
      <div className={`msg ${error ? 'err' : 'ok'}`}>{error || message || ''}</div>
      <div className="ftr">
        <span>ENTER=Submit</span>
        {onBack && <span className="pf" onClick={onBack}>PF3=Exit</span>}
        {s && <span className="pf" onClick={async () => {
          try { await api.post('/api/auth/logout') } catch (e) {}
          clearSession(); nav('/login')
        }}>Sign Off</span>}
      </div>
    </div>
  )
}

export function Field({ label, children }) {
  return (
    <div className="field">
      <label>{label}</label>
      <div className="fv">{children}</div>
    </div>
  )
}

export function RO({ label, value }) {
  return (
    <div className="field">
      <label>{label}</label>
      <div className="fv ro">{value ?? ''}</div>
    </div>
  )
}

export function Pager({ page, hasPrev, hasNext, onPage, startId, onStartId, startLabel }) {
  return (
    <div className="pager">
      {onStartId && (
        <span>Start {startLabel} <input value={startId} onChange={e => onStartId(e.target.value)} style={{ width: 130 }} /></span>
      )}
      <button disabled={!hasPrev} onClick={() => onPage(page - 1)}>PF7 Prev</button>
      <span> Page {page + 1} </span>
      <button disabled={!hasNext} onClick={() => onPage(page + 1)}>PF8 Next</button>
    </div>
  )
}

export function ErrText({ e }) { return e ? <span className="inline-err">{e}</span> : null }
