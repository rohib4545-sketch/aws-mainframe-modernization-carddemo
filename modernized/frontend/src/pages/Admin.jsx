import React, { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { Screen, Field, RO, Pager } from '../ui'

const userFields = [
  ['userId', 'User ID', 8], ['firstName', 'First Name', 20], ['lastName', 'Last Name', 20],
  ['password', 'Password', 8], ['userType', 'User Type (A/U)', 1],
]

/** COUSR00C — User List (admin). */
export function UserList() {
  const nav = useNavigate()
  const [startId, setStartId] = useState('')
  const [page, setPage] = useState(0)
  const [d, setD] = useState({ users: [] })
  const [err, setErr] = useState('')

  function load(p = page, s = startId) {
    setErr('')
    api.get(`/api/users?page=${p}${s ? '&startId=' + s : ''}`).then(setD).catch(e => setErr(e.message))
  }
  useEffect(() => { load(0) }, [])

  return (
    <Screen tranId="CU00" program="COUSR00C" title="User List" error={err} onBack={() => nav('/admin/menu')}>
      <table>
        <thead><tr><th>Sel</th><th>User ID</th><th>First Name</th><th>Last Name</th><th>Type</th></tr></thead>
        <tbody>
          {(d.users || []).map(u => (
            <tr key={u.userId}>
              <td>
                <a href="#" onClick={e => { e.preventDefault(); nav(`/admin/users/update?userId=${u.userId}`) }}>U</a>{' '}
                <a href="#" onClick={e => { e.preventDefault(); nav(`/admin/users/delete?userId=${u.userId}`) }}>D</a>
              </td>
              <td>{u.userId}</td><td>{u.firstName}</td><td>{u.lastName}</td><td>{u.userType}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pager page={page} hasPrev={d.hasPrevPage} hasNext={d.hasNextPage}
             onPage={p => { setPage(p); load(p) }}
             startId={startId} onStartId={v => { setStartId(v); setPage(0); load(0, v) }} startLabel="User ID" />
      <div className="legend">U=Update  D=Delete</div>
    </Screen>
  )
}

/** COUSR01C — Add user. */
export function UserAdd() {
  const nav = useNavigate()
  const [f, setF] = useState({})
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')

  async function submit(e) {
    e.preventDefault()
    setErr(''); setMsg('')
    try { const r = await api.post('/api/users', f); setMsg(r.message); setF({}) }
    catch (ex) { setErr(ex.message) }
  }
  return (
    <Screen tranId="CU01" program="COUSR01C" title="Add User" message={msg} error={err} onBack={() => nav('/admin/menu')}>
      <form onSubmit={submit}>
        {userFields.map(([k, l, m]) => (
          <Field key={k} label={l}>
            <input value={f[k] ?? ''} onChange={e => setF({ ...f, [k]: e.target.value })} maxLength={m}
                   type={k === 'password' ? 'password' : 'text'} />
          </Field>
        ))}
        <div style={{ marginLeft: 250 }}><button>ENTER - Add</button></div>
      </form>
    </Screen>
  )
}

/** COUSR02C — Update user. */
export function UserUpdate() {
  const nav = useNavigate()
  const [sp] = useSearchParams()
  const [userId, setUserId] = useState(sp.get('userId') || '')
  const [f, setF] = useState(null)
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')

  function find(e) {
    e?.preventDefault()
    setErr(''); setMsg('')
    api.get(`/api/users/${userId}`).then(u => setF(u)).catch(ex => setErr(ex.message))
  }
  useEffect(() => { if (userId) find() }, [])

  async function save(e) {
    e.preventDefault()
    setErr(''); setMsg('')
    try { const r = await api.put(`/api/users/${f.userId}`, f); setMsg(r.message); setF(r.user) }
    catch (ex) { setErr(ex.message) }
  }

  return (
    <Screen tranId="CU02" program="COUSR02C" title="Update User" message={msg} error={err} onBack={() => nav(-1)}>
      {!f ? (
        <form onSubmit={find}>
          <Field label="User ID"><input value={userId} onChange={e => setUserId(e.target.value)} maxLength={8} /></Field>
          <div style={{ marginLeft: 250 }}><button>ENTER</button></div>
        </form>
      ) : (
        <form onSubmit={save}>
          <RO label="User ID" value={f.userId} />
          <Field label="First Name"><input value={f.firstName ?? ''} onChange={e => setF({ ...f, firstName: e.target.value })} maxLength={20} /></Field>
          <Field label="Last Name"><input value={f.lastName ?? ''} onChange={e => setF({ ...f, lastName: e.target.value })} maxLength={20} /></Field>
          <Field label="Password"><input type="password" value={f.password ?? ''} onChange={e => setF({ ...f, password: e.target.value })} maxLength={8} /></Field>
          <Field label="User Type (A/U)"><input value={f.userType ?? ''} onChange={e => setF({ ...f, userType: e.target.value })} maxLength={1} style={{ width: 40 }} /></Field>
          <div style={{ marginLeft: 250 }}><button>ENTER - Update</button></div>
        </form>
      )}
    </Screen>
  )
}

/** COUSR03C — Delete user. */
export function UserDelete() {
  const nav = useNavigate()
  const [sp] = useSearchParams()
  const [userId, setUserId] = useState(sp.get('userId') || '')
  const [u, setU] = useState(null)
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')

  function find(e) {
    e?.preventDefault()
    setErr(''); setMsg('')
    api.get(`/api/users/${userId}`).then(setU).catch(ex => setErr(ex.message))
  }
  useEffect(() => { if (userId) find() }, [])

  async function del() {
    setErr(''); setMsg('')
    try { const r = await api.del(`/api/users/${u.userId}`); setMsg(r.message); setU(null) }
    catch (ex) { setErr(ex.message) }
  }

  return (
    <Screen tranId="CU03" program="COUSR03C" title="Delete User" message={msg} error={err} onBack={() => nav(-1)}>
      {!u ? (
        <form onSubmit={find}>
          <Field label="User ID"><input value={userId} onChange={e => setUserId(e.target.value)} maxLength={8} /></Field>
          <div style={{ marginLeft: 250 }}><button>ENTER</button></div>
        </form>
      ) : (
        <>
          <div className="grid2">
            <RO label="User ID" value={u.userId} />
            <RO label="Name" value={`${u.firstName} ${u.lastName}`} />
            <RO label="User Type" value={u.userType} />
          </div>
          <div style={{ margin: '16px 0 0 250px' }}>
            <button onClick={del}>DELETE</button>
            <button onClick={() => setU(null)}>Cancel</button>
          </div>
        </>
      )}
    </Screen>
  )
}

/** COTRTLIS / COTRTUP — Transaction Type list + update (Db2 maintenance). */
export function TranTypes() {
  const nav = useNavigate()
  const [rows, setRows] = useState([])
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')

  function load() { api.get('/api/tran-types').then(setRows).catch(e => setErr(e.message)) }
  useEffect(() => { load() }, [])

  async function save(t) {
    setErr(''); setMsg('')
    try { const r = await api.put(`/api/tran-types/${t.typeCd}`, t); setMsg(r.message); load() }
    catch (ex) { setErr(ex.message) }
  }

  return (
    <Screen tranId="CTTY" program="COTRTLIS" title="Transaction Type Maintenance" message={msg} error={err} onBack={() => nav('/admin/menu')}>
      <table>
        <thead><tr><th>Type CD</th><th>Description</th><th></th></tr></thead>
        <tbody>
          {rows.map(t => (
            <tr key={t.typeCd}>
              <td>{t.typeCd}</td>
              <td><input value={t.desc ?? ''} onChange={e => setRows(rows.map(r => r.typeCd === t.typeCd ? { ...r, desc: e.target.value } : r))} maxLength={50} /></td>
              <td><button onClick={() => save(t)}>Update</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </Screen>
  )
}

export function TranTypeAdd() {
  const nav = useNavigate()
  const [f, setF] = useState({})
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')
  async function submit(e) {
    e.preventDefault()
    setErr(''); setMsg('')
    try { const r = await api.post('/api/tran-types', f); setMsg(r.message); setF({}) }
    catch (ex) { setErr(ex.message) }
  }
  return (
    <Screen tranId="CTTA" program="COTRTUP" title="Add Tran Type" message={msg} error={err} onBack={() => nav('/admin/menu')}>
      <form onSubmit={submit}>
        <Field label="Type CD"><input value={f.typeCd ?? ''} onChange={e => setF({ ...f, typeCd: e.target.value })} maxLength={2} style={{ width: 50 }} /></Field>
        <Field label="Description"><input value={f.desc ?? ''} onChange={e => setF({ ...f, desc: e.target.value })} maxLength={50} /></Field>
        <div style={{ marginLeft: 250 }}><button>ENTER - Add</button></div>
      </form>
    </Screen>
  )
}

/** Batch: interest calculation + statement generation. */
export function BatchInterest() {
  const nav = useNavigate()
  const [r, setR] = useState(null)
  const [err, setErr] = useState('')
  async function run() {
    setErr('')
    try { setR(await api.post('/api/batch/interest', {})) }
    catch (ex) { setErr(ex.message) }
  }
  return (
    <Screen tranId="CBIN" program="CBACT04C" title="Interest Calculation" error={err} onBack={() => nav('/admin/menu')}>
      <p>Compute monthly interest across all account groups and post interest transactions (equivalent of batch job CBACT04C / INTCALC).</p>
      <button onClick={run}>Run Interest Calculation</button>
      {r && <div className="msg ok">Interest transactions written: {r.interestTransactions}, total interest: {r.totalInterest}</div>}
    </Screen>
  )
}

export function BatchStatement() {
  const nav = useNavigate()
  const [acctId, setAcctId] = useState('')
  const [startDate, setStart] = useState('')
  const [endDate, setEnd] = useState('')
  const [out, setOut] = useState('')
  const [err, setErr] = useState('')
  async function run(e) {
    e.preventDefault()
    setErr('')
    try {
      const d = await api.post(`/api/batch/statement/${acctId}`, { startDate, endDate })
      setOut(d.statement)
    } catch (ex) { setErr(ex.message) }
  }
  return (
    <Screen tranId="CBST" program="CBSTM03A" title="Statement Generation" error={err} onBack={() => nav('/admin/menu')}>
      <form onSubmit={run}>
        <Field label="Account ID"><input value={acctId} onChange={e => setAcctId(e.target.value)} maxLength={11} /></Field>
        <Field label="Start Date"><input value={startDate} onChange={e => setStart(e.target.value)} placeholder="YYYY-MM-DD" /></Field>
        <Field label="End Date"><input value={endDate} onChange={e => setEnd(e.target.value)} placeholder="YYYY-MM-DD" /></Field>
        <div style={{ marginLeft: 250 }}><button>Generate</button></div>
      </form>
      {out && <pre className="rpt">{out}</pre>}
    </Screen>
  )
}
