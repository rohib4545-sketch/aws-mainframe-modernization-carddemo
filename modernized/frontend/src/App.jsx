import React, { useEffect } from 'react'
import { Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { getSession, isAdmin, applyRoleTheme } from './api'
import Login from './pages/Login'
import { UserMenu, AdminMenu } from './pages/Menus'
import { AccountView, AccountUpdate } from './pages/Accounts'
import { CardList, CardView, CardUpdate } from './pages/Cards'
import { TranList, TranView, TranAdd, Reports } from './pages/Transactions'
import BillPay from './pages/BillPay'
import { UserList, UserAdd, UserUpdate, UserDelete, TranTypes, TranTypeAdd, BatchInterest, BatchStatement } from './pages/Admin'
import { AuthSummary, AuthDetail } from './pages/Authorizations'

function guard(el, adminOnly = false) {
  if (!getSession()) return <Navigate to="/login" replace />
  if (adminOnly && !isAdmin()) return <Navigate to="/menu" replace />
  return el
}

export default function App() {
  // Session changes always coincide with navigation (login/logout), so
  // re-apply the role theme whenever the route changes.
  const location = useLocation()
  useEffect(applyRoleTheme, [location])

  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/menu" element={guard(<UserMenu />)} />
      <Route path="/admin/menu" element={guard(<AdminMenu />, true)} />
      <Route path="/account/view" element={guard(<AccountView />)} />
      <Route path="/account/update" element={guard(<AccountUpdate />)} />
      <Route path="/cards" element={guard(<CardList />)} />
      <Route path="/cards/view" element={guard(<CardView />)} />
      <Route path="/cards/update" element={guard(<CardUpdate />)} />
      <Route path="/transactions" element={guard(<TranList />)} />
      <Route path="/transactions/view" element={guard(<TranView />)} />
      <Route path="/transactions/add" element={guard(<TranAdd />)} />
      <Route path="/reports" element={guard(<Reports />)} />
      <Route path="/bill-payment" element={guard(<BillPay />)} />
      <Route path="/authorizations" element={guard(<AuthSummary />)} />
      <Route path="/authorizations/:cardNum" element={guard(<AuthDetail />)} />
      <Route path="/admin/users" element={guard(<UserList />, true)} />
      <Route path="/admin/users/add" element={guard(<UserAdd />, true)} />
      <Route path="/admin/users/update" element={guard(<UserUpdate />, true)} />
      <Route path="/admin/users/delete" element={guard(<UserDelete />, true)} />
      <Route path="/admin/tran-types" element={guard(<TranTypes />, true)} />
      <Route path="/admin/tran-types/add" element={guard(<TranTypeAdd />, true)} />
      <Route path="/admin/batch/interest" element={guard(<BatchInterest />, true)} />
      <Route path="/admin/batch/statement" element={guard(<BatchStatement />, true)} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
