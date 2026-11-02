import { useEffect, useRef } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { Icon } from './ui'

export default function ProfileMenu({ name, onSignOut }) {
  const menu = useRef(null)
  const location = useLocation()
  useEffect(() => { menu.current.open = false }, [location.pathname, location.search])
  useEffect(() => {
    function dismiss(event) {
      if (menu.current && !menu.current.contains(event.target)) menu.current.open = false
    }
    document.addEventListener('pointerdown', dismiss)
    return () => document.removeEventListener('pointerdown', dismiss)
  }, [])
  return <details ref={menu} className="profile-menu" onKeyDown={event => {
    if (event.key === 'Escape') { menu.current.open = false; menu.current.querySelector('summary').focus() }
  }}>
    <summary aria-label={`Profile options for ${name}`}><span className="avatar">{name.slice(0, 1).toUpperCase()}</span><span className="profile-name"><strong>{name}</strong><small>Profile & settings</small></span><span aria-hidden="true">⌄</span></summary>
    <div className="profile-options"><NavLink to="/settings" onClick={() => { menu.current.open = false }}><Icon name="settings"/>Settings</NavLink><NavLink to="/about" onClick={() => { menu.current.open = false }}><Icon name="book"/>About</NavLink><button onClick={onSignOut}><Icon name="exit"/>Log out</button></div>
  </details>
}
