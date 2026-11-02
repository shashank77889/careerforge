import { useResource } from './useResource'
import { useState } from 'react'
import { api, userPath, userKey, readSaved } from './api'
import { Icon, PageHeading, ErrorNotice, Empty, Loading } from './ui'

export default function Goals() {
  const { data: goals, loading, error, reload, setData } = useResource(userPath('career-goals'))
  const [draft, setDraft] = useState(() => readSaved('goalDraft', {}))
  const [showForm, setShowForm] = useState(() => Boolean(readSaved('goalDraft', {}).targetRole))
  const [saving, setSaving] = useState(false)
  const [pending, setPending] = useState([])
  const [feedback, setFeedback] = useState('')
  const [filter, setFilter] = useState('All')
  const completed = goals.filter(g => g.completed).length
  const progress = goals.length ? Math.round(completed / goals.length * 100) : 0
  async function add(event) {
    event.preventDefault()
    if (saving) return
    const form = Object.fromEntries(new FormData(event.currentTarget))
    if (!form.targetRole.trim()) { setFeedback('Please describe your goal.'); return }
    setSaving(true); setFeedback('')
    try {
      const goal = await api(userPath('career-goals'), { method: 'POST', body: JSON.stringify({ ...form, targetRole: form.targetRole.trim(), completed: false, skills: '', preferredLocation: '' }) })
      setData(previous => [...previous, goal]); setShowForm(false); setDraft({}); localStorage.removeItem(userKey('goalDraft'))
    } catch (err) { setFeedback(err.message) } finally { setSaving(false) }
  }
  async function toggle(goal) {
    if (pending.includes(goal.id)) return
    setPending(previous => [...previous, goal.id]); setFeedback('')
    try {
      const saved = await api(`/career-goals/${goal.id}/completion?completed=${!goal.completed}`, { method: 'PUT' })
      setData(previous => previous.map(item => item.id === goal.id ? saved : item))
    } catch (err) { setFeedback(err.message) } finally { setPending(previous => previous.filter(id => id !== goal.id)) }
  }
  const filtered = goals.filter(g => filter === 'All' || (filter === 'Completed' ? g.completed : !g.completed))
  return <><PageHeading eyebrow="GIVE YOUR AMBITION A DIRECTION" title="Career goals" description="Big ambitions, manageable steps. Build a path that feels like you."><button className="primary" onClick={() => setShowForm(!showForm)}>{showForm ? 'Close form' : <><Icon name="plus"/> New goal</>}</button></PageHeading><ErrorNotice message={error} retry={reload}/><ErrorNotice message={feedback}/><section className="panel goal-summary"><div><span className="eyebrow">YOUR PROGRESS</span><h2>{completed} <span className="muted">of {goals.length} goals completed</span></h2><p>{progress === 100 ? 'Take a moment to celebrate. Then dream a little bigger.' : 'Progress is built one small action at a time.'}</p></div><div className="summary-progress"><strong>{progress}%</strong><progress max="100" value={progress} aria-label="Goals completed"/></div></section>{showForm && <section className="panel form-panel"><h2>What’s your next milestone?</h2><form onSubmit={add} onChange={event => { const next = Object.fromEntries(new FormData(event.currentTarget)); setDraft(next); try { localStorage.setItem(userKey('goalDraft'), JSON.stringify(next)) } catch { /* Form input remains available in memory. */ } }}><label>Your goal<input defaultValue={draft.targetRole || ""} name="targetRole" autoFocus required maxLength={255} placeholder="e.g. Build and publish my portfolio"/></label><label>Category<select defaultValue={draft.employmentType || "Career"} name="employmentType">{['Career', 'Technical Skills', 'Education', 'Interview Preparation', 'Personal Development'].map(c => <option key={c}>{c}</option>)}</select></label><div className="form-actions"><button className="primary" disabled={saving}>{saving ? 'Saving…' : 'Create goal'}</button><button type="button" className="secondary" disabled={saving} onClick={() => setShowForm(false)}>Cancel</button></div></form></section>}<div className="section-title"><h2>Your milestones</h2><div className="tabs" aria-label="Filter goals">{['All', 'In progress', 'Completed'].map(tab => <button key={tab} aria-pressed={filter === tab} className={filter === tab ? 'active' : ''} onClick={() => setFilter(tab)}>{tab}</button>)}</div></div>{loading ? <Loading/> : !error && (filtered.length ? <div className="goal-list">{filtered.map(goal => <article className={`goal-row ${goal.completed ? 'is-complete' : ''}`} key={goal.id}><button className="goal-toggle" aria-label={`${goal.completed ? 'Reopen' : 'Complete'} ${goal.targetRole}`} aria-pressed={goal.completed} disabled={pending.includes(goal.id)} onClick={() => toggle(goal)}>{goal.completed && <Icon name="check" size={17}/>}</button><div><span className="eyebrow">{goal.employmentType || 'Career'}</span><h3>{goal.targetRole}</h3></div><span className={`pill ${goal.completed ? 'mint' : ''}`}>{pending.includes(goal.id) ? 'Saving…' : goal.completed ? 'Completed' : 'In progress'}</span></article>)}</div> : <Empty icon="goal" title={goals.length ? 'No goals in this view' : 'Every journey starts with a goal'} description={goals.length ? 'Choose another filter to see your milestones.' : 'A new skill, a new role, a fresh start. What will you work toward?'}><button className="primary" onClick={() => goals.length ? setFilter('All') : setShowForm(true)}>{goals.length ? 'View all goals' : 'Create your first goal'}</button></Empty>)}</>
}


