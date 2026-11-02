import { PageHeading } from './ui'
import AccountControls from './AccountControls'
import { Link } from 'react-router-dom'

export default function Settings({ theme, onThemeChange, onDeleted }) {
  return <>
    <PageHeading eyebrow="YOUR WORKSPACE" title="Settings" description="Make CareerForge comfortable for you."/>
    <div className="settings-layout">
      <section className="panel settings-section" aria-labelledby="appearance-title">
        <h2 id="appearance-title">Appearance</h2>
        <p>Choose your theme. Your preference is remembered for this account on this browser.</p>
        <fieldset className="theme-options"><legend className="sr-only">Color theme</legend>
          {['light', 'dark'].map(value => <label key={value} className={`theme-choice ${value}`}>
            <span className="theme-preview" aria-hidden="true"><i/><span><b/><b/><b/></span></span>
            <span><input type="radio" name="theme" value={value} checked={theme === value} onChange={() => onThemeChange(value)}/>{value === 'light' ? 'Light' : 'Dark'}</span>
          </label>)}
        </fieldset>
      </section>
      <section className="panel settings-section" aria-labelledby="account-title">
        <h2 id="account-title">Account</h2><strong>{localStorage.getItem('userName') || 'Your account'}</strong>
        <p className="account-email">{localStorage.getItem('userEmail')}</p><Link className="text-link" to="/forgot-password">Reset my password</Link>
      </section>
      <AccountControls onDeleted={onDeleted}/>
    </div>

  </>
}
