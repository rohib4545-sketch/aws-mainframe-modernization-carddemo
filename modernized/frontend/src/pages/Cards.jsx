import React, { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { Screen, Field, RO, Pager } from '../ui'

/** COCRDLIC — Credit Card List, 7 rows/page with account & card-number filters. */
export function CardList() {
  const nav = useNavigate()
  const [acctId, setAcctId] = useState('')
  const [cardNum, setCardNum] = useState('')
  const [page, setPage] = useState(0)
  const [d, setD] = useState({ cards: [] })
  const [err, setErr] = useState('')

  function load(p = page, a = acctId, c = cardNum) {
    setErr('')
    const q = new URLSearchParams({ page: p })
    if (a) q.set('acctId', a)
    if (c) q.set('cardNum', c)
    api.get(`/api/cards?${q}`).then(setD).catch(e => { setErr(e.message); setD({ cards: [] }) })
  }
  useEffect(() => { load(0) }, [])

  return (
    <Screen tranId="CCLI" program="COCRDLIC" title="Credit Card List" error={err} onBack={() => nav('/menu')}>
      <form onSubmit={e => { e.preventDefault(); setPage(0); load(0) }} style={{ display: 'flex', gap: 16, marginBottom: 8 }}>
        <span>Acct ID <input value={acctId} onChange={e => setAcctId(e.target.value)} maxLength={11} style={{ width: 120 }} /></span>
        <span>Card Number <input value={cardNum} onChange={e => setCardNum(e.target.value)} maxLength={16} style={{ width: 170 }} /></span>
        <button>ENTER</button>
      </form>
      <table>
        <thead><tr><th>Sel</th><th>Card Number</th><th>Acct ID</th><th>Card Status</th><th>Name on Card</th><th>Expiry</th></tr></thead>
        <tbody>
          {(d.cards || []).map(c => (
            <tr key={c.cardNum}>
              <td>
                <a href="#" onClick={e => { e.preventDefault(); nav(`/cards/view?cardNum=${c.cardNum}`) }}>S</a>{' '}
                <a href="#" onClick={e => { e.preventDefault(); nav(`/cards/update?cardNum=${c.cardNum}`) }}>U</a>
              </td>
              <td>{c.cardNum}</td><td>{c.acctId}</td><td>{c.activeStatus}</td>
              <td>{c.embossedName}</td><td>{c.expirationDate}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pager page={page} hasPrev={d.hasPrevPage} hasNext={d.hasNextPage}
             onPage={p => { setPage(p); load(p) }} />
      <div style={{ color: '#6e7681', fontSize: 12 }}>S=View  U=Update</div>
    </Screen>
  )
}

/** COCRDSL — Credit Card View. */
export function CardView() {
  const nav = useNavigate()
  const [sp] = useSearchParams()
  const [acctId, setAcctId] = useState('')
  const [cardNum, setCardNum] = useState(sp.get('cardNum') || '')
  const [d, setD] = useState(null)
  const [err, setErr] = useState('')

  function find(e) {
    e?.preventDefault()
    setErr('')
    const q = cardNum ? `cardNum=${cardNum}` : `acctId=${acctId}`
    api.get(`/api/cards/view?${q}`).then(setD).catch(ex => { setErr(ex.message); setD(null) })
  }
  useEffect(() => { if (cardNum) find() }, [])

  const c = d?.card, a = d?.account, cust = d?.customer
  return (
    <Screen tranId="CCDL" program="COCRDSLC" title="Credit Card View" error={err} onBack={() => nav(-1)}>
      <form onSubmit={find} style={{ display: 'flex', gap: 16, marginBottom: 12 }}>
        <span>Acct ID <input value={acctId} onChange={e => setAcctId(e.target.value)} maxLength={11} style={{ width: 120 }} /></span>
        <span>Card Number <input value={cardNum} onChange={e => setCardNum(e.target.value)} maxLength={16} style={{ width: 170 }} /></span>
        <button>ENTER</button>
      </form>
      {c && (
        <div className="grid2">
          <RO label="Card Number" value={c.cardNum} />
          <RO label="Account ID" value={c.acctId} />
          <RO label="Name on Card" value={c.embossedName} />
          <RO label="Card Status" value={c.activeStatus} />
          <RO label="CVV" value={c.cvvCode} />
          <RO label="Expiry Date" value={c.expirationDate} />
          {a && <RO label="Account Balance" value={a.currBal} />}
          {a && <RO label="Account Status" value={a.activeStatus} />}
          {cust && <RO label="Customer" value={`${cust.firstName} ${cust.lastName}`} />}
        </div>
      )}
    </Screen>
  )
}

/** COCRDUP — Credit Card Update. */
export function CardUpdate() {
  const nav = useNavigate()
  const [sp] = useSearchParams()
  const [cardNum, setCardNum] = useState(sp.get('cardNum') || '')
  const [d, setD] = useState(null)
  const [f, setF] = useState({})
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')

  function find(e) {
    e?.preventDefault()
    setErr(''); setMsg('')
    api.get(`/api/cards/view?cardNum=${cardNum}`).then(res => {
      setD(res)
      const [y, m] = (res.card.expirationDate || '').split('-')
      setF({ embossedName: res.card.embossedName, activeStatus: res.card.activeStatus, expMonth: m, expYear: y })
    }).catch(ex => { setErr(ex.message); setD(null) })
  }
  useEffect(() => { if (cardNum) find() }, [])

  async function save(e) {
    e.preventDefault()
    setErr(''); setMsg('')
    try {
      const r = await api.put(`/api/cards/${d.card.cardNum}`, f)
      setMsg(r.message)
      setD({ ...d, card: r.card })
    } catch (ex) { setErr(ex.message) }
  }

  const c = d?.card, a = d?.account
  return (
    <Screen tranId="CCUP" program="COCRDUPC" title="Credit Card Update" message={msg} error={err} onBack={() => nav(-1)}>
      {!d ? (
        <form onSubmit={find}>
          <Field label="Card Number"><input value={cardNum} onChange={e => setCardNum(e.target.value)} maxLength={16} /></Field>
          <div style={{ marginLeft: 250 }}><button>ENTER</button></div>
        </form>
      ) : (
        <form onSubmit={save}>
          <RO label="Card Number" value={c.cardNum} />
          {a && <RO label="Account ID" value={a.acctId} />}
          <Field label="Name on Card"><input value={f.embossedName || ''} onChange={e => setF({ ...f, embossedName: e.target.value })} maxLength={50} /></Field>
          <Field label="Card Status (Y/N)"><input value={f.activeStatus || ''} onChange={e => setF({ ...f, activeStatus: e.target.value })} maxLength={1} style={{ width: 40 }} /></Field>
          <Field label="Expiry Month"><input value={f.expMonth || ''} onChange={e => setF({ ...f, expMonth: e.target.value })} maxLength={2} style={{ width: 50 }} /></Field>
          <Field label="Expiry Year"><input value={f.expYear || ''} onChange={e => setF({ ...f, expYear: e.target.value })} maxLength={4} style={{ width: 70 }} /></Field>
          <div style={{ marginTop: 12 }}>
            <button type="submit">ENTER - Update</button>
            <button type="button" onClick={() => { setD(null); setCardNum('') }}>New Search</button>
          </div>
        </form>
      )}
    </Screen>
  )
}
