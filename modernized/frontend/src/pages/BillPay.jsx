import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { Screen, Field, RO } from '../ui'

/** COBIL00C — Bill Payment: enter acct, see balance, confirm Y/N. */
export default function BillPay() {
  const nav = useNavigate()
  const [acctId, setAcctId] = useState('')
  const [bal, setBal] = useState(null)
  const [confirm, setConfirm] = useState('')
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')

  async function find(e) {
    e.preventDefault()
    setErr(''); setMsg('')
    try {
      const d = await api.get(`/api/accounts/${acctId}/bill-payment`)
      setBal(d.currentBalance); setMsg(d.message)
    } catch (ex) { setErr(ex.message); setBal(null) }
  }

  async function pay(c) {
    setErr(''); setMsg('')
    try {
      const d = await api.post(`/api/accounts/${acctId}/bill-payment`, { confirm: c })
      setMsg(d.message)
      if (d.newBalance !== undefined) setBal(d.newBalance)
    } catch (ex) { setErr(ex.message) }
  }

  return (
    <Screen tranId="CB00" program="COBIL00C" title="Bill Payment" message={msg} error={err} onBack={() => nav('/menu')}>
      <form onSubmit={find}>
        <Field label="Account ID"><input value={acctId} onChange={e => setAcctId(e.target.value)} maxLength={11} /></Field>
        <div style={{ marginLeft: 250 }}><button>ENTER</button></div>
      </form>
      {bal !== null && (
        <>
          <RO label="Current Balance" value={bal} />
          <Field label="Confirm payment (Y/N)">
            <input value={confirm} onChange={e => setConfirm(e.target.value)} maxLength={1} style={{ width: 40 }} />
          </Field>
          <div style={{ marginLeft: 250 }}>
            <button onClick={() => pay(confirm || 'Y')}>Pay Full Balance</button>
            <button onClick={() => pay('N')}>Cancel</button>
          </div>
        </>
      )}
    </Screen>
  )
}
