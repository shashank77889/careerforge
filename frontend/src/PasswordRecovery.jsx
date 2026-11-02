import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { api, clearSession } from './api'
import { ErrorNotice } from './ui'

export default function PasswordRecovery({ onReset }) {
  const location = useLocation()
  const reset = location.pathname === '/reset-password'
  const [token] = useState(() => new URLSearchParams(location.hash.slice(1)).get('token') || '')
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [complete, setComplete] = useState(false)
  const [visible, setVisible] = useState(false)

  async function submit(event) {
    event.preventDefault()
    if (busy) return
    const values = Object.fromEntries(new FormData(event.currentTarget))
    setError('')
    if (reset && values.password !== values.confirm) { setError('The passwords do not match.'); return }
    if (reset && new TextEncoder().encode(values.password).length > 72) { setError('Please use a shorter password (at most 72 UTF-8 bytes).'); return }
    setBusy(true)
    try {
      const data = await api(reset ? '/users/reset-password' : '/users/forgot-password', {
        method: 'POST', body: JSON.stringify(reset ? { token, password: values.password } : { email: values.email.trim() }),
      })
      setMessage(data.message); setComplete(true)
      if (reset) { clearSession(); onReset(); window.history.replaceState(null, '', '/reset-password') }
    } catch (err) { setError(err.message) } finally { setBusy(false) }
  }
  return <div className="auth-page"><section className="auth-story">
    <Link to="/" className="brand-lockup"><span className="brand-mark">c<span>f</span></span>CareerForge</Link>
    <div className="auth-pitch"><span className="eyebrow">PICK UP WHERE YOU LEFT OFF</span><h1>A fresh password.<br/><em>The same progress.</em></h1><p>Your goals, study timetable, and career progress stay with your account.</p></div>
    <p className="auth-footnote">Your potential. Your pace. Your path.</p>
  </section><section className="auth-form-wrap"><div className="auth-form">
    <span className="pill">ACCOUNT RECOVERY</span><h2>{complete ? reset ? 'You’re ready to sign in.' : 'Check your inbox.' : reset ? 'Choose a new password.' : 'Forgot your password?'}</h2>
    <p>{reset ? 'Use a password you haven’t used for another account. Resetting signs you out on other devices.' : 'Enter the email address you used to create your account. We’ll email you a link to reset your password.'}</p>
    <ErrorNotice message={error}/>
    {message && <p className="notice success" role="status">{message}</p>}
    {reset && !token && !complete ? <p className="notice" role="alert">This link is missing its reset code. <Link to="/forgot-password">Request a new link</Link>.</p> : !complete && <form onSubmit={submit}>
      {reset ? <><label>New password<div className="password-field"><input name="password" type={visible ? 'text' : 'password'} autoComplete="new-password" minLength={8} required placeholder="At least 8 characters"/><button type="button" className="text-button" onClick={() => setVisible(!visible)}>{visible ? 'Hide' : 'Show'}</button></div></label><label>Confirm new password<input name="confirm" type={visible ? 'text' : 'password'} autoComplete="new-password" minLength={8} required/></label></> : <label>Email address<input name="email" type="email" autoComplete="email" maxLength={254} required placeholder="you@example.com"/></label>}
      <button className="primary auth-submit" disabled={busy}>{busy ? 'Please wait…' : reset ? 'Reset password' : 'Send reset link'}</button>
    </form>}
    <p className="auth-switch"><Link to="/">Back to sign in</Link></p>
    {reset && error && <p><Link to="/forgot-password">Request a new reset link</Link></p>}
    {!reset && complete && <button className="text-button" onClick={() => { setComplete(false); setMessage('') }}>Try another email address</button>}
  </div></section></div>
}
