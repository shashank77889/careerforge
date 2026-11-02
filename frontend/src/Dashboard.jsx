import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from './api'
import { useResource } from './useResource'
import { Icon, PageHeading, ErrorNotice, Loading } from './ui'
import Onboarding from './Onboarding'

function welcome(now) {
  const hour = now.getHours()
  return hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening'
}

function localMoment(now) {
  return new Intl.DateTimeFormat(undefined, { weekday: 'long', day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit' }).format(now)
}

export default function Dashboard() {
  const journey = useResource('/journey')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [now, setNow] = useState(() => new Date())
  const navigate = useNavigate()
  const name = (localStorage.getItem('userName') || 'there').split(' ')[0]
  useEffect(() => { const timer = setInterval(() => setNow(new Date()), 30000); return () => clearInterval(timer) }, [])
  async function buildPlan() {
    setBusy(true); setError('')
    try { await api('/journey/plan',{method:'POST'}); navigate('/learning') }
    catch (err) { setError(err.message) } finally { setBusy(false) }
  }
  if (journey.loading) return <Loading/>
  if (journey.error) return <ErrorNotice message={journey.error} retry={journey.reload}/>
  const data = journey.data
  if (!data.configured) return <><PageHeading eyebrow="YOUR FIRST STEPS" title={'Welcome, ' + name + '.'} description="Set up your workspace in three short steps."/><Onboarding journey={data} onSaved={journey.setData}/></>
  const next = data.plan.find(step => step.status === 'IN_PROGRESS') || data.plan.find(step => step.status !== 'COMPLETED')
  return <>
    <PageHeading eyebrow={localMoment(now).toUpperCase()} title={welcome(now) + ', ' + name + '.'} description="Pick up where you left off."/>
    <ErrorNotice message={error}/>
    <section className="panel daily-focus">
      <div><span className="eyebrow">{data.role} · {data.weeklyHours}h / week</span><h2>{next ? 'Continue with ' + next.skill : data.total ? 'Your learning path is complete' : 'Build your learning plan'}</h2>
      <p>{next ? next.resource.practice : data.total ? 'Review what you can do independently, then practise for your next interview.' : 'Review your skills to create a plan for your target role.'}</p>
      {next ? <Link className="primary" to="/learning">Continue preparation <Icon name="arrow"/></Link> : data.total ? <Link className="primary" to="/interviews">Practise an interview <Icon name="arrow"/></Link> : <button className="primary" disabled={busy} onClick={buildPlan}>{busy ? 'Building your plan…' : 'Create my learning plan'}<Icon name="arrow"/></button>}
      </div><div className="daily-progress"><strong>{data.completed || 0}<span> / {data.total} milestones</span></strong><progress aria-label="Learning milestones completed" value={data.completed || 0} max={data.total || 1}/><small>Completed learning tasks</small></div>
    </section>
    <nav className="dashboard-secondary-links" aria-label="Other preparation options"><Link to="/job-match">Check a job match <Icon name="arrow" size={16}/></Link><Link to="/interviews">Practice an interview <Icon name="arrow" size={16}/></Link></nav>
  </>
}