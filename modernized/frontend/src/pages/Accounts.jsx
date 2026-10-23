import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { Screen, Field, RO } from '../ui'

function AcctSearch({ onFound, label }) {
  const [acctId, setAcctId] = useState('')
  const [cardNum, setCardNum] = useState('')
  const [err, setErr] = useState('')
  async function go(e) {
    e.preventDefault()
    setErr('')
    try {
      const q = acctId ? `acctId=${acctId}` : `cardNum=${cardNum}`
      const d = await api.get(`/api/accounts?${q}`)
      onFound(d)
    } catch (ex) { setErr(ex.message) }
  }
  return (
    <>
      <form onSubmit={go}>
        <Field label={label || 'Account Number'}>
          <input value={acctId} onChange={e => setAcctId(e.target.value)} maxLength={11} />
        </Field>
        <Field label="OR Card Number">
          <input value={cardNum} onChange={e => setCardNum(e.target.value)} maxLength={16} />
        </Field>
        <div style={{ marginLeft: 250 }}><button>ENTER</button></div>
      </form>
      {err && <div className="inline-err">{err}</div>}
    </>
  )
}

/** COACTVWC — Account View. */
export function AccountView() {
  const nav = useNavigate()
  const [d, setD] = useState(null)
  const a = d?.account, c = d?.customer
  return (
    <Screen tranId="CAVW" program="COACTVWC" title="Account View" onBack={() => nav('/menu')}>
      {!d ? <AcctSearch onFound={setD} /> : (
        <>
          <h3 className="sect">Account</h3>
          <div className="grid2">
            <RO label="Account ID" value={a.acctId} />
            <RO label="Active Status" value={a.activeStatus} />
            <RO label="Current Balance" value={a.currBal} />
            <RO label="Credit Limit" value={a.creditLimit} />
            <RO label="Cash Credit Limit" value={a.cashCreditLimit} />
            <RO label="Open Date" value={a.openDate} />
            <RO label="Expiry Date" value={a.expirationDate} />
            <RO label="Reissue Date" value={a.reissueDate} />
            <RO label="Cycle Credit" value={a.currCycCredit} />
            <RO label="Cycle Debit" value={a.currCycDebit} />
            <RO label="Group ID" value={a.groupId} />
          </div>
          {c && <>
            <h3 className="sect">Customer</h3>
            <div className="grid2">
              <RO label="Customer ID" value={c.custId} />
              <RO label="Name" value={`${c.firstName} ${c.middleName} ${c.lastName}`} />
              <RO label="SSN" value={c.ssn} />
              <RO label="DOB" value={c.dob} />
              <RO label="FICO Score" value={c.ficoCreditScore} />
              <RO label="Address" value={`${c.addrLine1}, ${c.addrLine3}`} />
              <RO label="State / Country / Zip" value={`${c.addrStateCd} / ${c.addrCountryCd} / ${c.addrZip}`} />
              <RO label="Phone" value={c.phoneNum1} />
            </div>
          </>}
          <button onClick={() => setD(null)}>New Search</button>
        </>
      )}
    </Screen>
  )
}

/** COACTUPC — Account Update. */
export function AccountUpdate() {
  const nav = useNavigate()
  const [d, setD] = useState(null)
  const [f, setF] = useState({})
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')

  function load(data) {
    setD(data)
    const a = data.account, c = data.customer || {}
    setF({
      activeStatus: a.activeStatus, openDate: a.openDate, expirationDate: a.expirationDate,
      reissueDate: a.reissueDate, creditLimit: a.creditLimit, cashCreditLimit: a.cashCreditLimit,
      currBal: a.currBal, currCycCredit: a.currCycCredit, currCycDebit: a.currCycDebit,
      addrZip: a.addrZip, groupId: a.groupId,
      firstName: c.firstName, middleName: c.middleName, lastName: c.lastName,
      addrLine1: c.addrLine1, addrLine2: c.addrLine2, addrLine3: c.addrLine3,
      addrStateCd: c.addrStateCd, addrCountryCd: c.addrCountryCd, addrZipCust: c.addrZip,
      phoneNum1: c.phoneNum1, phoneNum2: c.phoneNum2, ssn: c.ssn, govtIssuedId: c.govtIssuedId,
      dob: c.dob, eftAccountId: c.eftAccountId, priCardHolderInd: c.priCardHolderInd,
      ficoCreditScore: c.ficoCreditScore,
    })
  }

  async function save(e) {
    e.preventDefault()
    setErr(''); setMsg('')
    try {
      const r = await api.put(`/api/accounts/${d.account.acctId}`, f)
      setMsg(r.message); load(r)
    } catch (ex) { setErr(ex.message) }
  }

  const i = (k, max = 50) =>
    <input value={f[k] ?? ''} onChange={e => setF({ ...f, [k]: e.target.value })} maxLength={max} />

  return (
    <Screen tranId="CAUP" program="COACTUPC" title="Account Update" message={msg} error={err}
            onBack={() => nav('/menu')}>
      {!d ? <AcctSearch onFound={load} label="Account ID to update" /> : (
        <form onSubmit={save}>
          <h3 className="sect">Account {d.account.acctId}</h3>
          <div className="grid2">
            <Field label="Active Status (Y/N)">{i('activeStatus', 1)}</Field>
            <Field label="Open Date">{i('openDate', 10)}</Field>
            <Field label="Expiry Date">{i('expirationDate', 10)}</Field>
            <Field label="Reissue Date">{i('reissueDate', 10)}</Field>
            <Field label="Credit Limit">{i('creditLimit', 12)}</Field>
            <Field label="Cash Credit Limit">{i('cashCreditLimit', 12)}</Field>
            <Field label="Current Balance">{i('currBal', 12)}</Field>
            <Field label="Curr Cyc Credit">{i('currCycCredit', 12)}</Field>
            <Field label="Curr Cyc Debit">{i('currCycDebit', 12)}</Field>
            <Field label="Group ID">{i('groupId', 10)}</Field>
          </div>
          {d.customer && <>
            <h3 className="sect">Customer {d.customer.custId}</h3>
            <div className="grid2">
              <Field label="First Name">{i('firstName', 25)}</Field>
              <Field label="Middle Name">{i('middleName', 25)}</Field>
              <Field label="Last Name">{i('lastName', 25)}</Field>
              <Field label="SSN">{i('ssn', 9)}</Field>
              <Field label="DOB (YYYY-MM-DD)">{i('dob', 10)}</Field>
              <Field label="FICO Score">{i('ficoCreditScore', 3)}</Field>
              <Field label="Address Line 1">{i('addrLine1')}</Field>
              <Field label="Address Line 2">{i('addrLine2')}</Field>
              <Field label="City">{i('addrLine3')}</Field>
              <Field label="State">{i('addrStateCd', 2)}</Field>
              <Field label="Country">{i('addrCountryCd', 3)}</Field>
              <Field label="Zip">{i('addrZipCust', 10)}</Field>
              <Field label="Phone 1">{i('phoneNum1', 15)}</Field>
              <Field label="Phone 2">{i('phoneNum2', 15)}</Field>
              <Field label="EFT Account ID">{i('eftAccountId', 10)}</Field>
              <Field label="Primary Card Holder (Y/N)">{i('priCardHolderInd', 1)}</Field>
            </div>
          </>}
          <div style={{ marginTop: 14 }}>
            <button type="submit">ENTER - Update</button>
            <button type="button" onClick={() => { setD(null); setMsg(''); setErr('') }}>New Search</button>
          </div>
        </form>
      )}
    </Screen>
  )
}
