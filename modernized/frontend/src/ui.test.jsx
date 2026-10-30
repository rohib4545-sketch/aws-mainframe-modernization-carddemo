import { describe, it, expect, beforeEach } from 'vitest'
import React from 'react'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { Screen, Field, RO, Pager, ErrText } from './ui'
import { setSession } from './api'

beforeEach(() => sessionStorage.clear())

function renderInRouter(ui) {
  return render(<MemoryRouter>{ui}</MemoryRouter>)
}

describe('Screen', () => {
  it('renders tran id, program and title in the 3270 header', () => {
    renderInRouter(<Screen tranId="CC00" program="COSGN00C" title="Signon Screen">body</Screen>)
    expect(screen.getByText(/Tran: CC00/)).toBeTruthy()
    expect(screen.getByText(/Prog: COSGN00C/)).toBeTruthy()
    expect(screen.getByText('Signon Screen')).toBeTruthy()
  })

  it('shows the signed-on user and a Sign Off key when a session exists', () => {
    setSession({ token: 't', userId: 'USER0001', userType: 'U' })
    renderInRouter(<Screen tranId="CM00" program="COMEN01C">body</Screen>)
    expect(screen.getByText(/Signed on as USER0001/)).toBeTruthy()
    expect(screen.getByText('Sign Off')).toBeTruthy()
  })

  it('shows error text in the message line', () => {
    renderInRouter(<Screen tranId="X" program="Y" error="Wrong Password">body</Screen>)
    expect(screen.getByText('Wrong Password')).toBeTruthy()
  })
})

describe('Field and RO', () => {
  it('renders a labelled field wrapper', () => {
    render(<Field label="User ID"><input /></Field>)
    expect(screen.getByText('User ID')).toBeTruthy()
  })

  it('renders a read-only value', () => {
    render(<RO label="Acct" value="00000000001" />)
    expect(screen.getByText('00000000001')).toBeTruthy()
  })
})

describe('Pager', () => {
  it('disables Prev on page 0 and Next when hasNext is false', () => {
    render(<Pager page={0} hasPrev={false} hasNext={false} onPage={() => {}} />)
    expect(screen.getByText('PF7 Prev').disabled).toBe(true)
    expect(screen.getByText('PF8 Next').disabled).toBe(true)
    expect(screen.getByText(/Page 1/)).toBeTruthy()
  })

  it('enables paging buttons when pages exist', () => {
    render(<Pager page={1} hasPrev hasNext onPage={() => {}} />)
    expect(screen.getByText('PF7 Prev').disabled).toBe(false)
    expect(screen.getByText('PF8 Next').disabled).toBe(false)
  })
})

describe('ErrText', () => {
  it('renders nothing when there is no error', () => {
    const { container } = render(<ErrText e={null} />)
    expect(container.firstChild).toBeNull()
  })
})
