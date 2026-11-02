import { useEffect, useState } from 'react'
import { NavLink, Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import Dashboard from './Dashboard'
import WorkspaceBoundary from './WorkspaceBoundary'
import Research from './Research'
import Settings from './Settings'
import About from './About'
import ProfileMenu from './ProfileMenu'
import Applications from './Applications'
import MyPlan from './MyPlan'
import FloatingCoach from './FloatingCoach'
import Learning from './Learning'
import Interviews from './Interviews'
import JobMatch from './JobMatch'
import PasswordRecovery from './PasswordRecovery'
import { api, clearSession } from './api'
import { Icon, ErrorNotice } from './ui'
import './App.css'
import './Journey.css'
import './Polish.css'
import './Theme.css'
import './Experience.css'
import './Typography.css'
import './WorkspaceFocus.css'
import './Settings.css'
import './Simplify.css'
import './Motion.css'

const navigation = [['/dashboard', 'Overview', 'grid'], ['/job-match', 'Job Match', 'search'], ['/learning', 'Skills & Learning', 'book'], ['/interviews', 'Mock Interviews', 'briefcase']]
const workspacePaths = [...navigation.map(([path]) => path), '/applications', '/skills', '/goals']

function Auth({ onLogin, expired }) {
  const [register, setRegister] = useState(false)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [visible, setVisible] = useState(false)
  async function submit(event) {
    event.preventDefault()
    if (busy) return
    const values = Object.fromEntries(new FormData(event.currentTarget))
    values.email = values.email.trim()
    if (register && !values.name.trim()) { setError('Please enter your name.'); return }
    setBusy(true); setError(''); setMessage('')
    try {
      if (register) {
        await api('/users/register', { method: 'POST', body: JSON.stringify({ ...values, name: values.name.trim() }) })
        setRegister(false); setMessage('Your account is ready. Sign in to get started.')
      } else {
        const data = await api('/users/login', { method: 'POST', body: JSON.stringify(values) })
        if (!data?.token || !data.id) throw new Error('Unable to sign in. Please try again.')
        for (const [key, value] of Object.entries({ token: data.token, userId: data.id, userName: data.name, userEmail: data.email })) localStorage.setItem(key, value)
        await onLogin()
      }
    } catch (err) { setError(err.message) } finally { setBusy(false) }
  }
  return <div className="auth-page"><section className="auth-story"><NavLink to="/dashboard" className="brand-lockup" aria-label="CareerForge home"><span className="brand-mark">c<span>f</span></span>CareerForge</NavLink><div className="auth-pitch"><span className="eyebrow">A LITTLE PROGRESS. EVERY DAY.</span><h1>Your next chapter<br/>starts <em>here.</em></h1><p>Turn your ambition into a plan. Build your skills, track opportunities, and move toward a career you love.</p><div className="journey-art" aria-hidden="true"><div className="orbit orbit-one"/><div className="orbit orbit-two"/><div className="art-core"><Icon name="goal" size={56}/></div><span className="art-tag tag-one"><Icon name="check"/> One step closer</span><span className="art-tag tag-two"><Icon name="chart"/> Room to grow</span></div></div><p className="auth-footnote">Your potential. Your pace. Your path.</p></section><section className="auth-form-wrap"><div className="auth-form"><span className="pill">YOUR CAREER, CONNECTED</span><h2>{register ? 'Make room for possibility.' : 'Good to see you again.'}</h2><p>{register ? 'Create your workspace and take the first step.' : 'Sign in and pick up where you left off.'}</p>{expired && <p className="notice">Your session expired. Please sign in again.</p>}<ErrorNotice message={error}/>{message && <p className="notice success" role="status">{message}</p>}<form onSubmit={submit}>{register && <label>Full name<input name="name" autoComplete="name" required maxLength={100} placeholder="Your name"/></label>}<label>Email address<input name="email" type="email" autoComplete="email" required placeholder="you@example.com"/></label><label>Password<div className="password-field"><input name="password" type={visible ? 'text' : 'password'} autoComplete={register ? 'new-password' : 'current-password'} minLength={register ? 8 : undefined} required placeholder={register ? 'At least 8 characters' : 'Enter your password'}/><button type="button" className="text-button" aria-label={visible ? 'Hide password' : 'Show password'} onClick={() => setVisible(!visible)}>{visible ? 'Hide' : 'Show'}</button></div></label>{!register && <p className="auth-switch"><NavLink to="/forgot-password">Forgot password?</NavLink></p>}<button className="primary auth-submit" disabled={busy}>{busy ? 'Please wait…' : register ? 'Create account' : 'Sign in'}<Icon name="arrow"/></button></form><p className="auth-switch">{register ? 'Already have an account?' : 'New to CareerForge?'} <button className="text-button" disabled={busy} onClick={() => { setRegister(!register); setError(''); setMessage('') }}>{register ? 'Sign in' : 'Create an account'}</button></p><div className="auth-benefits"><span>Goals with direction</span><span>Skills that matter</span></div></div></section></div>
}

export default function App() {
  const [signedIn, setSignedIn] = useState(() => Boolean(localStorage.getItem('token') && localStorage.getItem('userId')))
  const [expired, setExpired] = useState(false)
  const [themes, setThemes] = useState({})
  const themeKey = `careerforge-theme:${signedIn ? localStorage.getItem('userId') : 'guest'}`
  const theme = themes[themeKey] || (localStorage.getItem(themeKey) === 'dark' ? 'dark' : 'light')
  useEffect(() => { document.documentElement.dataset.theme = theme }, [theme])
  function changeTheme(value) {
    setThemes(previous => ({ ...previous, [themeKey]: value }))
    localStorage.setItem(themeKey, value)
  }
  const navigate = useNavigate()
  const location = useLocation()
  const title = ({ '/forgot-password': 'Forgot password', '/reset-password': 'Reset password', '/goals': 'My Plan', '/applications': 'Applications', '/settings': 'Settings', '/about': 'About', '/research': 'Research lab' })[location.pathname] || navigation.find(([path]) => path === location.pathname)?.[1] || 'Overview'
  useEffect(() => {
    const expire = () => { setSignedIn(false); setExpired(true) }
    window.addEventListener('session-expired', expire)
    return () => window.removeEventListener('session-expired', expire)
  }, [])
  useEffect(() => { document.title = `${signedIn ? title : 'Welcome'} · CareerForge`; window.scrollTo(0, 0) }, [title, signedIn])
  useEffect(() => {
    if (!signedIn || !workspacePaths.includes(location.pathname)) return
    api('/journey/activity', { method: 'PUT', body: JSON.stringify({ path: location.pathname }) }).catch(() => { /* Navigation stays available during a temporary connection failure. */ })
  }, [signedIn, location.pathname])
  if (['/forgot-password', '/reset-password'].includes(location.pathname)) return <PasswordRecovery key={location.pathname} onReset={() => { setSignedIn(false); setExpired(false) }}/>
  if (!signedIn) return <Auth expired={expired} onLogin={async () => {
    let destination = '/dashboard'
    try { const journey = await api('/journey'); if (workspacePaths.includes(journey.lastPage)) destination = journey.lastPage } catch { /* Other pages can still be opened if the journey service is unavailable. */ }
    if (!localStorage.getItem('token')) return
    setSignedIn(true); setExpired(false); navigate(destination)
  }}/>
  const signOut = () => { clearSession(); setSignedIn(false); setExpired(false); navigate('/') }
  const name = localStorage.getItem('userName') || 'Your workspace'
  return <div className="app-shell"><a className="skip-link" href="#main">Skip to content</a><aside className="sidebar"><NavLink to="/dashboard" className="brand-lockup" aria-label="CareerForge home"><span className="brand-mark">c<span>f</span></span>CareerForge</NavLink><span className="nav-caption">WORKSPACE</span><nav aria-label="Main navigation">{navigation.map(([path, label, icon]) => <NavLink key={path} to={path}><Icon name={icon}/><span>{label}</span></NavLink>)}</nav><details className="secondary-navigation" open={['/applications', '/goals'].includes(location.pathname) || undefined}><summary>More</summary><NavLink to="/applications"><Icon name="briefcase"/> Applications</NavLink><NavLink to="/goals"><Icon name="goal"/> Goals & timetable</NavLink></details><ProfileMenu name={name} onSignOut={signOut}/></aside><div className="workspace"><header className="topbar"><span>Workspace <span className="breadcrumb-divider">/</span> <strong>{title}</strong></span><span className="workspace-status"><i/> Your career workspace</span></header><main key={location.pathname} id="main" className="page-transition" tabIndex={-1}><WorkspaceBoundary><Routes><Route path="/research" element={<Research/>}/><Route path="/settings" element={<Settings theme={theme} onThemeChange={changeTheme} onDeleted={signOut}/>}/><Route path="/about" element={<About/>}/><Route path="/dashboard" element={<Dashboard/>}/><Route path="/goals" element={<MyPlan key={location.search}/>}/><Route path="/applications" element={<Applications/>}/><Route path="/job-match" element={<JobMatch/>}/><Route path="/skills" element={<Navigate to="/learning?view=skills" replace/>}/><Route path="/learning" element={<Learning key={location.search}/>}/><Route path="/interviews" element={<Interviews/>}/><Route path="*" element={<Navigate to="/dashboard" replace/>}/></Routes></WorkspaceBoundary></main><FloatingCoach/></div></div>
}


