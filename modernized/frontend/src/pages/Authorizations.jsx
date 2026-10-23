import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { Screen, RO, Pager } from '../ui'

/** COAUTH00 — Pending authorizations summary (IMS/DB2 optional module). */
export function AuthSummary() {
  const nav = useNavigate()
  const [page, setPage] = useState(0)
  const [d, setD] = useState({ authorizations: [] })
  const [err, setErr] = useState('')

  useEffect(() => {
    api.get(`/api/authorizations?page=${page}`).then(setD).catch(e => setErr(e.message))
  }, [page])

  return (
    <Screen tranId="CAUTH" program="COAUTH00" title="Pending Authorizations" error={err} onBack={() => nav('/menu')}>
      <table>
        <thead><tr><th>Account</th><th>Approved</th><th>Declined</th><th>Auth Amount</th><th>Unmatched</th><th>Fraud</th></tr></thead>
        <tbody>
          {(d.authorizations || []).map(r => (
            <tr key={r.acctId}>
              <td><a href="#" onClick={e => { e.preventDefault(); nav(`/authorizations/${r.acctId}`) }}>{r.acctId}</a></td>
              <td>{r.approvedCount}</td><td>{r.declinedCount}</td>
              <td>{r.authAmount}</td><td>{r.unmatchedCount}</td><td>{r.fraudCount}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pager page={page} hasPrev={d.hasPrevPage} hasNext={d.hasNextPage} onPage={setPage} />
    </Screen>
  )
}

export function AuthDetail() {
  const nav = useNavigate()
  const [acctId, setAcctId] = useState('')
  const [rows, setRows] = useState([])
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')

  function load(id) {
    setErr('')
    api.get(`/api/authorizations/${id}`).then(d => setRows(d.authorizations)).catch(e => setErr(e.message))
  }

  async function act(a, action) {
    setErr(''); setMsg('')
    try {
      const r = action === 'fraud'
        ? await api.post(`/api/authorizations/${a.cardNum}/${encodeURIComponent(a.authTs)}/fraud`, { fraud: 'Y' })
        : await api.post(`/api/authorizations/${a.cardNum}/${encodeURIComponent(a.authTs)}/match`, {})
      setMsg(r.message); load(a.acctId)
    } catch (ex) { setErr(ex.message) }
  }

  return (
    <Screen tranId="CAUD" program="COAUTDTL" title="Authorization Detail" message={msg} error={err} onBack={() => nav(-1)}>
      <form onSubmit={e => { e.preventDefault(); load(acctId) }} style={{ display: 'flex', gap: 10, marginBottom: 12 }}>
        <span>Acct ID <input value={acctId} onChange={e => setAcctId(e.target.value)} maxLength={11} /></span>
        <button>ENTER</button>
      </form>
      <table>
        <thead><tr><th>Card</th><th>Auth TS</th><th>Amount</th><th>Resp</th><th>Merchant</th><th>Match</th><th>Fraud</th><th>Actions</th></tr></thead>
        <tbody>
          {rows.map(a => (
            <tr key={a.cardNum + a.authTs}>
              <td>{a.cardNum}</td><td>{a.authTs}</td><td>{a.amt}</td><td>{a.respCode}</td>
              <td>{a.merchantName}</td><td>{a.matchedStatus}</td><td>{a.fraudFlag}</td>
              <td>
                <button onClick={() => act(a, 'match')}>Match</button>
                <button onClick={() => act(a, 'fraud')}>Fraud</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </Screen>
  )
}
