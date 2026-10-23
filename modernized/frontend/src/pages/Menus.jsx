import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'
import { Screen } from '../ui'

/** COMEN01C / COADM01C — menus driven by the API's option list (numbered, like the BMS maps). */
function Menu({ tranId, program, title, backTo }) {
  const nav = useNavigate()
  const [opts, setOpts] = useState([])
  const [sel, setSel] = useState('')
  const [err, setErr] = useState('')

  useEffect(() => {
    api.get('/api/menus').then(d => setOpts(d.options)).catch(e => setErr(e.message))
  }, [])

  function go(optNum) {
    const opt = opts.find(o => o.num === Number(optNum))
    if (!opt) { setErr('Please enter a valid option number...'); return }
    nav(opt.route)
  }

  return (
    <Screen tranId={tranId} program={program} title={title} error={err}
            onBack={() => nav(backTo)}>
      <div className="menu-opts">
        {opts.map(o => (
          <div key={o.num} onClick={() => nav(o.route)}>
            <span className="num">{String(o.num).padStart(2, '0')}.</span>
            <span>{o.name}</span>
          </div>
        ))}
      </div>
      <form onSubmit={e => { e.preventDefault(); go(sel) }} style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
        <span>Select Option:</span>
        <input value={sel} onChange={e => setSel(e.target.value)} style={{ width: 60 }} />
        <button type="submit">ENTER</button>
      </form>
    </Screen>
  )
}

export function UserMenu() {
  return <Menu tranId="CM00" program="COMEN01C" title="Main Menu" backTo="/login" />
}
export function AdminMenu() {
  return <Menu tranId="CA00" program="COADM01C" title="Admin Menu" backTo="/login" />
}
