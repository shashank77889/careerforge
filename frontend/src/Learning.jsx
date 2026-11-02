import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import Skills from './Skills'
import VideoResources from './VideoResources'
import { api } from './api'
import { useResource } from './useResource'
import { Icon, PageHeading, ErrorNotice, Loading, Empty } from './ui'
import { JourneySettings } from './JourneyWidgets'

function Milestone({ step, pending, onUpdate }) {
  const [reviewing,setReviewing] = useState(false)
  const complete = step.status === 'COMPLETED'
  return <article className={`roadmap-milestone ${complete ? 'is-done' : ''} ${step.status === 'IN_PROGRESS' ? 'is-current' : ''}`}>
    <div className="milestone-marker">{complete ? <Icon name="check" size={18}/> : <span>{step.week}</span>}</div>
    <div className="milestone-content"><div className="milestone-top"><span className="eyebrow">WEEK {step.week} · {step.resource.hours}H ESTIMATED PRACTICE</span><span className={`pill ${complete ? 'mint' : step.status === 'IN_PROGRESS' ? 'lavender' : ''}`}>{complete ? 'Completed' : step.status === 'IN_PROGRESS' ? 'In progress' : 'Up next'}</span></div>
      <h3>{step.skill}</h3><p className="milestone-reason">{step.reason}</p>
      <div className="practice-brief"><span><Icon name="goal" size={16}/> PUT IT INTO PRACTICE</span><p>{step.resource.practice}</p></div>
      <details className="learning-resources"><summary>Learning resources & prerequisites</summary>      <div className="source-trust"><span className="verified-check"><Icon name="check" size={13}/></span><div><strong>{step.resource.provider}</strong><small>{step.resource.format} · Editorially reviewed by CareerForge</small></div></div>
      {step.resource.prerequisites.length > 0 && <div className="prerequisite-line">Helpful foundations: {step.resource.prerequisites.join(' · ')}</div>}
      <VideoResources skill={step.skill}/><a className="text-link" href={step.resource.url} target="_blank" rel="noopener noreferrer">Open learning resource ↗</a></details>
      <div className="milestone-actions"><button className={complete ? 'secondary' : 'primary'} disabled={pending} onClick={() => step.status === 'IN_PROGRESS' ? setReviewing(true) : onUpdate(step, 'IN_PROGRESS', step.reflection)}>{pending ? 'Saving…' : complete ? 'Reopen milestone' : step.status === 'IN_PROGRESS' ? 'Review practical work' : 'Start milestone'}</button></div>
      {reviewing && !complete && <form className="practice-assessment" onSubmit={event => { event.preventDefault(); const form = new FormData(event.currentTarget); onUpdate(step,'COMPLETED',form.get('reflection')) }}><h4>A quick practice review</h4><p>{step.resource.practice}</p><label>What did you build, and how did you test it?<textarea name="reflection" required minLength={40} maxLength={2000} rows={4} defaultValue={step.reflection || ''} placeholder="Example: I built a task manager and tested empty inputs and duplicate tasks. I still need to practise error handling."/></label><label className="consent-check"><input type="checkbox" required/>I attempted the practical task and can explain my own approach.</label><small>This is a self-assessment, not an independently graded certification. Skill ratings do not change automatically.</small><div className="milestone-actions"><button className="primary" disabled={pending}>Save & complete</button><button type="button" className="secondary" onClick={() => setReviewing(false)}>Keep practicing</button></div></form>}
      <details className="practice-notes"><summary>{step.reflection ? 'View or edit your practice notes' : 'Add practice notes or a project reference'}</summary><form key={step.reflection} onSubmit={event => {event.preventDefault(); onUpdate(step, step.status, new FormData(event.currentTarget).get('reflection'))}}><label>What did you build or learn?<textarea name="reflection" defaultValue={step.reflection} maxLength={2000} rows="3" placeholder="Describe your work, add a repository reference, and note what you still need to practice."/></label><button className="secondary" disabled={pending}>Save notes</button></form></details>
    </div>
  </article>
}

export default function Learning() {
  const [params, setParams] = useSearchParams()
  const view = params.get('view') === 'skills' ? 'skills' : 'courses'
  const resource = useResource('/journey')
  const [pending, setPending] = useState(null)
  const [error, setError] = useState('')
  const [filter, setFilter] = useState('All milestones')
  const [query, setQuery] = useState('')
  const [message, setMessage] = useState('')
  async function generate() {
    setPending('generate'); setError(''); setMessage('')
    try { resource.setData(await api('/journey/plan',{method:'POST'})); setMessage('Your path is up to date. Existing progress and notes have been preserved.') }
    catch (err) { setError(err.message) } finally { setPending(null) }
  }
  async function update(step,status,reflection) {
    if (pending) return
    setPending(step.id); setError(''); setMessage('')
    try { resource.setData(await api(`/journey/plan/${step.id}`,{method:'PUT',body:JSON.stringify({status,reflection})})); setMessage(status === 'COMPLETED' ? `${step.skill} milestone completed. Reflect on your practice before updating proficiency.` : 'Progress saved to your account.') }
    catch (err) { setError(err.message) } finally { setPending(null) }
  }
  function exportPlan() {
    const data = resource.data
    const lines = [`# My ${data.role} learning path`, `${data.weeklyHours} study hours per week. Estimated practice time, not a job-readiness deadline.`, '', ...data.plan.flatMap(step => [`## Week ${step.week}: ${step.skill}`, `Status: ${step.status}`, `Practice: ${step.resource.practice}`, `Resource: ${step.resource.url}`, `Notes: ${step.reflection || 'No notes yet.'}`, ''])]
    const url = URL.createObjectURL(new Blob([lines.join('\n')],{type:'text/markdown;charset=utf-8'}))
    const link = document.createElement('a'); link.href = url; link.download = 'careerforge-learning-path.md'; link.click(); setTimeout(() => URL.revokeObjectURL(url),1000)
  }
  if (resource.loading) return <Loading/>
  if (resource.error) return <ErrorNotice message={resource.error} retry={resource.reload}/>
  const data = resource.data
  const progress = data.total ? Math.round(data.completed / data.total * 100) : 0
  const next = data.plan.find(step => step.skill === params.get('skill')) || data.plan.find(step => step.status === 'IN_PROGRESS') || data.plan.find(step => step.status !== 'COMPLETED')
  const filtered = data.plan.filter(step => (filter !== 'Completed' || step.status === 'COMPLETED') && (filter !== 'To do' || step.status !== 'COMPLETED') && step.skill.toLowerCase().includes(query.toLowerCase()))
  return <>
    <PageHeading eyebrow="PREPARATION" title="Skills & Learning" description="Review what you know, find the gaps, and practice the skills your role needs.">{view === 'courses' && <div className="heading-actions">{data.total > 0 && <button className="secondary" onClick={exportPlan}>Export path ↓</button>}<button className="secondary" disabled={Boolean(pending)} onClick={generate}>{pending === 'generate' ? 'Building…' : data.total ? 'Refresh my path' : 'Generate my path'}<Icon name="arrow" size={16}/></button></div>}</PageHeading>
    <nav className="workspace-tabs" aria-label="Skills and learning views"><button aria-pressed={view === 'skills'} onClick={() => setParams({view:'skills'})}>Resume & skill gaps</button><button aria-pressed={view === 'courses'} onClick={() => { resource.reload(); setParams({view:'courses'}) }}>Learning path</button></nav>
    {view === 'skills' ? <Skills embedded/> : <>
    <details className="learning-options"><summary>Plan settings & optional timetable</summary><Link className="text-link" to="/goals?view=timetable">Open study timetable ↗</Link><JourneySettings journey={data} onSave={value => {resource.setData(value); setMessage('Direction saved. Generate or refresh your path for this role.')}}/></details>
    <ErrorNotice message={error}/>{message && <div className="notice success" role="status">{message}</div>}
    <div className="learning-progress-line"><strong>{data.role}</strong><span>{data.completed}/{data.total} tasks completed</span><progress value={progress} max="100" aria-label="Learning path completed"/></div>
    {data.total > 0 && data.completed === data.total && <section className="path-celebration"><span>✦</span><div><h2>You followed through. That matters.</h2><p>Every milestone is complete. Now review what you can do independently, update your proficiency, and save a new readiness snapshot.</p></div><Link className="primary" to="/skills">Review my growth <Icon name="arrow"/></Link></section>}
    {next && <section className="next-learning-task" aria-label="Your next learning task"><div className="section-title"><div><span className="eyebrow">YOUR NEXT STEP</span><h2>Focus on {next.skill}</h2></div></div><Milestone key={next.id} step={next} pending={Boolean(pending)} onUpdate={update}/></section>}
    {data.total > 0 ? <details className="full-learning-path"><summary>View full learning path · {data.completed}/{data.total} completed</summary>
    <div className="section-title resource-toolbar"><div className="tabs">{['All milestones','To do','Completed'].map(value => <button key={value} aria-pressed={filter === value} className={filter === value ? 'active' : ''} onClick={() => setFilter(value)}>{value}</button>)}</div><div className="search-field"><Icon name="search" size={18}/><input aria-label="Search roadmap" value={query} onChange={e => setQuery(e.target.value)} placeholder="Find a skill in your path…"/></div></div>
    {filtered.length ? <div className="roadmap-layout"><div className="roadmap-timeline">{filtered.map(step => <Milestone key={step.id} step={step} pending={Boolean(pending)} onUpdate={update}/>)}</div><aside className="roadmap-aside"><span className="eyebrow">LEARN → PRACTICE → REFLECT</span><h3>Make it stick.</h3><p>Following a tutorial is a start. Rebuild a small example without instructions, explain your choices, and test an edge case.</p><hr/><h3>Keep evidence of your work.</h3><p>Add a short reflection or project reference to each milestone. It will stay with your account across devices.</p><hr/><h3>Your pace can change.</h3><p>Time estimates are planning guides. Adjust your weekly hours whenever life gets busy.</p><Link className="text-link" to="/skills">Revisit skill ratings ↗</Link></aside></div> : <Empty icon="book" title={data.total ? 'No milestones in this view' : data.readiness.matched === data.readiness.skills.length ? 'Your ratings meet this role’s learning targets' : 'A path built around your starting point'} description={data.total ? 'Try another filter or search.' : data.readiness.matched === data.readiness.skills.length ? 'Keep practicing through portfolio projects. Review your ratings if they no longer reflect your experience.' : 'Generate a roadmap for missing and developing skills, with resources and practical milestones.'}><button className="primary" disabled={Boolean(pending)} onClick={data.total ? () => {setFilter('All milestones');setQuery('')} : generate}>{data.total ? 'Show all milestones' : 'Generate my path'}</button></Empty>}
    </details> : <Empty icon="book" title="Build your learning path" description="Start with the skills your target role needs."><button className="primary" disabled={Boolean(pending)} onClick={generate}>Generate my path</button></Empty>}
    </>}
    <p className="path-method">Personalized using your role, proficiency ratings, prerequisites, and study hours. Completing a milestone updates learning progress, not your skill proficiency or hiring prospects.</p>
  </>
}
