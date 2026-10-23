import React, { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { Screen, Field, RO, Pager } from '../ui'

/** COTRN00C — Transaction List, 10 rows/page, browsable by start id. */
export function TranList() {
  const nav = useNavigate()
  const [startId, setStartId] = useState('')
  const [page, setPage] = useState(0)
  const [d, setD] = useState({ transactions: [] })
  const [err, setErr] = useState('')

  function load(p = page, s = startId) {
    setErr('')
    api.get(`/api/transactions?page=${p}${s ? '&startId=' + s : ''}`)
      .then(setD).catch(e => setErr(e.message))
  }
  useEffect(() => { load(0) }, [])

  return (
    <Screen tranId="CT00" program="COTRN00C" title="Transaction List" error={err} onBack={() => nav('/menu')}>
      <table>
        <thead><tr><th>Sel</th><th>Tran ID</th><th>Date</th><th>Description</th><th>Amount</th></tr></thead>
        <tbody>
          {(d.transactions || []).map(t => (
            <tr key={t.tranId}>
              <td><a href="#" onClick={e => { e.preventDefault(); nav(`/transactions/view?tranId=${t.tranId}`) }}>S</a></td>
              <td>{t.tranId}</td>
              <td>{(t.origTs || '').slice(0, 10)}</td>
              <td>{t.desc}</td>
              <td>{t.amt}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pager page={page} hasPrev={d.hasPrevPage} hasNext={d.hasNextPage}
             onPage={p => { setPage(p); load(p) }}
             startId={startId} onStartId={v => { setStartId(v); setPage(0); load(0, v) }} startLabel="Tran ID" />
    </Screen>
  )
}

/** COTRN01C — Transaction View. */
export function TranView() {
  const nav = useNavigate()
  const [sp] = useSearchParams()
  const [tranId, setTranId] = useState(sp.get('tranId') || '')
  const [t, setT] = useState(null)
  const [err, setErr] = useState('')

  function find(e) {
    e?.preventDefault()
    setErr('')
    api.get(`/api/transactions/${tranId}`).then(setT).catch(ex => { setErr(ex.message); setT(null) })
  }
  useEffect(() => { if (tranId) find() }, [])

  return (
    <Screen tranId="CT01" program="COTRN01C" title="Transaction View" error={err} onBack={() => nav(-1)}>
      <form onSubmit={find} style={{ display: 'flex', gap: 10, marginBottom: 12 }}>
        <span>Tran ID <input value={tranId} onChange={e => setTranId(e.target.value)} maxLength={16} /></span>
        <button>ENTER</button>
      </form>
      {t && (
        <div className="grid2">
          <RO label="Tran ID" value={t.tranId} />
          <RO label="Card Number" value={t.cardNum} />
          <RO label="Type CD" value={t.typeCd} />
          <RO label="Category CD" value={t.catCd} />
          <RO label="Source" value={t.source} />
          <RO label="Amount" value={t.amt} />
          <RO label="Orig Date" value={t.origTs} />
          <RO label="Proc Date" value={t.procTs} />
          <RO label="Merchant ID" value={t.merchantId} />
          <RO label="Merchant Name" value={t.merchantName} />
          <RO label="Merchant City" value={t.merchantCity} />
          <RO label="Merchant Zip" value={t.merchantZip} />
          <div style={{ gridColumn: '1/-1' }}><RO label="Description" value={t.desc} /></div>
        </div>
      )}
    </Screen>
  )
}

/** COTRN02C — Transaction Add with confirm step (like the BMS Y/N confirm). */
export function TranAdd() {
  const nav = useNavigate()
  const [f, setF] = useState({ typeCd: '01', source: 'POS TERM' })
  const [cats, setCats] = useState([])
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')
  const [pending, setPending] = useState(false)

  useEffect(() => {
    api.get(`/api/tran-categories?typeCd=${f.typeCd || '01'}`).then(setCats).catch(() => {})
  }, [f.typeCd])

  async function submit(e, confirm) {
    e?.preventDefault()
    setErr(''); setMsg('')
    try {
      const r = await api.post('/api/transactions', { ...f, confirm })
      if (r.requiresConfirmation) { setPending(true); setMsg(r.message); return }
      setMsg(r.message); setPending(false)
      setF({ typeCd: '01', source: 'POS TERM' })
    } catch (ex) { setErr(ex.message) }
  }

  const i = (k, label, max = 50, w) => (
    <Field label={label}>
      <input value={f[k] ?? ''} onChange={e => setF({ ...f, [k]: e.target.value })} maxLength={max} style={w ? { width: w } : {}} />
    </Field>
  )

  return (
    <Screen tranId="CT02" program="COTRN02C" title="Transaction Add" message={msg} error={err} onBack={() => nav('/menu')}>
      <form onSubmit={e => submit(e, 'Y')}>
        <div className="grid2">
          {i('acctId', 'Account ID (or Card)', 11, 140)}
          {i('cardNum', 'Card Number', 16, 170)}
          <Field label="Type CD">
            <select value={f.typeCd} onChange={e => setF({ ...f, typeCd: e.target.value })}>
              {['01','02','03','04','05','06','07'].map(c => <option key={c} value={c}>{c}</option>)}
            </select>
          </Field>
          <Field label="Category CD">
            <select value={f.catCd ?? ''} onChange={e => setF({ ...f, catCd: e.target.value })}>
              <option value="">-- select --</option>
              {cats.map(c => <option key={c.catCd} value={c.catCd}>{c.catCd} - {c.desc}</option>)}
            </select>
          </Field>
          {i('source', 'Source', 10, 120)}
          {i('amt', 'Amount (-99999999.99)', 12, 140)}
          {i('origDate', 'Orig Date (YYYY-MM-DD)', 10, 130)}
          {i('procDate', 'Proc Date (YYYY-MM-DD)', 10, 130)}
          {i('merchantId', 'Merchant ID', 9, 120)}
          {i('merchantName', 'Merchant Name')}
          {i('merchantCity', 'Merchant City')}
          {i('merchantZip', 'Merchant Zip', 10, 120)}
        </div>
        {i('desc', 'Description', 100)}
        {!pending ? (
          <div style={{ marginTop: 12 }}>
            <button type="button" onClick={e => submit(e, '')}>ENTER - Review</button>
          </div>
        ) : (
          <div style={{ marginTop: 12 }}>
            <span style={{ marginRight: 16 }}>Confirm add? (Y/N)</span>
            <button type="submit">Y - Confirm</button>
            <button type="button" onClick={e => submit(e, 'N')}>N - Cancel</button>
          </div>
        )}
      </form>
    </Screen>
  )
}

/** CORPT00C — Transaction reports: monthly / yearly / custom range. */
export function Reports() {
  const nav = useNavigate()
  const [startDate, setStart] = useState('')
  const [endDate, setEnd] = useState('')
  const [d, setD] = useState(null)
  const [err, setErr] = useState('')

  function run(type) {
    setErr('')
    const q = type === 'custom' ? `&startDate=${startDate}&endDate=${endDate}` : ''
    api.get(`/api/reports/transactions?type=${type}${q}`).then(setD).catch(e => { setErr(e.message); setD(null) })
  }

  return (
    <Screen tranId="CR00" program="CORPT00C" title="Transaction Reports" error={err} onBack={() => nav('/menu')}>
      <form onSubmit={e => { e.preventDefault(); run('custom') }} style={{ display: 'flex', gap: 14, flexWrap: 'wrap', marginBottom: 14 }}>
        <label><input type="radio" name="rpt" onChange={() => run('monthly')} /> Monthly</label>
        <label><input type="radio" name="rpt" onChange={() => run('yearly')} /> Yearly</label>
        <label><input type="radio" name="rpt" onChange={() => {}} /> Custom</label>
        <span>Start <input value={startDate} onChange={e => setStart(e.target.value)} placeholder="YYYY-MM-DD" style={{ width: 120 }} /></span>
        <span>End <input value={endDate} onChange={e => setEnd(e.target.value)} placeholder="YYYY-MM-DD" style={{ width: 120 }} /></span>
        <button type="submit">Run Custom</button>
      </form>
      {d && (
        <>
          <h3 className="sect">{d.reportName} Report: {d.startDate} to {d.endDate} — {d.count} transactions, total {d.totalAmount}</h3>
          <table>
            <thead><tr><th>Tran ID</th><th>Date</th><th>Type</th><th>Cat</th><th>Card</th><th>Description</th><th>Amount</th></tr></thead>
            <tbody>
              {d.transactions.map(t => (
                <tr key={t.tranId}>
                  <td>{t.tranId}</td><td>{(t.origTs || '').slice(0, 10)}</td><td>{t.typeCd}</td>
                  <td>{t.catCd}</td><td>{t.cardNum}</td><td>{t.desc}</td><td>{t.amt}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </Screen>
  )
}
