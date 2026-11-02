import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from './api'
import { useResource } from './useResource'
import { Icon, ErrorNotice, Loading } from './ui'

export function JourneySettings({ journey, onSave }) {
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  async function submit(event) {
    event.preventDefault()
    const values = Object.fromEntries(new FormData(event.currentTarget))
    setSaving(true); setError('')
    try { onSave(await api('/journey', { method: 'PUT', body: JSON.stringify({ role: values.role, weeklyHours: Number(values.weeklyHours) }) })) }
    catch (err) { setError(err.message) } finally { setSaving(false) }
  }
  return <details className="journey-settings" open={!journey.configured || undefined}>
    <summary><span><Icon name="goal"/> {journey.configured ? 'Your direction & study schedule' : 'Start here: choose your direction'}</span><span>{journey.role} · {journey.weeklyHours}h / week</span></summary>
    <form onSubmit={submit} key={`${journey.role}-${journey.weeklyHours}`}>
      <label>Target career<select name="role" defaultValue={journey.role}>{journey.roles.map(role => <option key={role}>{role}</option>)}</select></label>
      <label>Hours per week<input name="weeklyHours" type="number" min="1" max="40" required defaultValue={journey.weeklyHours}/></label>
      <button className="primary" disabled={saving}>{saving ? 'Saving…' : 'Save direction'}</button>
    </form><ErrorNotice message={error}/>
    <p>Your learning plans are kept separately for each role. Choose a pace you can sustain.</p>
  </details>
}

export function ReadinessDial({ score, compact = false }) {
  return <div className={`readiness-dial ${compact ? 'compact' : ''}`} role="img" aria-label={`Self-reported skill readiness ${score} percent`}>
    <svg viewBox="0 0 160 160" aria-hidden="true"><circle cx="80" cy="80" r="68" className="dial-track"/><circle cx="80" cy="80" r="68" className="dial-value" strokeDasharray={`${score * 4.2726} 427.26`}/></svg>
    <div><strong>{score}<span>%</span></strong><small>SKILL READINESS</small></div>
  </div>
}

export function SkillMap({ readiness, limit, plan }) {
  const list = limit ? readiness.skills.slice(0, limit) : readiness.skills
  return <section className="panel skill-map"><div className="section-title"><div><span className="eyebrow">YOUR STARTING POINT</span><h2>Strengths & room to grow</h2></div><span className="map-legend"><i/> Current <b/> Target</span></div>
    {list.map(item => <div className="skill-comparison" key={item.skill}><div><strong>{item.skill}</strong><span>{item.current}% <small>/ {item.required}% target</small></span></div><div className="comparison-track" role="img" aria-label={`${item.skill}: ${item.current}% current, ${item.required}% target`}><span style={{width:`${item.current}%`}}/><i style={{left:`${item.required}%`}}/></div><small>{item.status === 'MATCHED' ? 'Meets your learning target' : item.status === 'MISSING' ? 'Not in your toolkit yet' : item.source === 'RESUME' && item.current === 0 ? 'Found in resume · proficiency needs your review' : 'Keep developing'}</small>{plan && item.status !== 'MATCHED' && <div className="gap-resource">{plan.find(step => step.skill === item.skill) ? <a className="text-link" href={plan.find(step => step.skill === item.skill).resource.url} target="_blank" rel="noopener noreferrer">Study {item.skill} · {plan.find(step => step.skill === item.skill).resource.provider} ↗</a> : <Link className="text-link" to="/learning?view=courses">Build a learning path for this gap ↗</Link>}</div>}</div>)}
    <details className="score-explainer"><summary>How is this score calculated?</summary><p>{readiness.basis}</p><p>Each skill contributes up to its role target. Listing a skill does not establish proficiency, and completing a learning milestone does not change this score.</p></details>
    {limit && <Link to="/skills" className="text-link">Review all skills <Icon name="arrow" size={16}/></Link>}
  </section>
}

export function CareerCoach() {
  const status = useResource('/journey/coach')
  const [question, setQuestion] = useState('')
  const [consent, setConsent] = useState(false)
  const [reply, setReply] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  async function ask(event) {
    event.preventDefault()
    if (!question.trim() || busy) return
    setBusy(true); setError('')
    try { setReply(await api('/journey/coach', { method:'POST', body:JSON.stringify({question:question.trim(),consent}) })) }
    catch (err) { setError(err.message) } finally { setBusy(false) }
  }
  if (status.loading) return <Loading/>
  if (status.error) return <ErrorNotice message={status.error} retry={status.reload}/>
  if (!status.data.available) return <section className="coach-unavailable"><h3>Need a hand?</h3><p>The AI coach isn’t connected yet. You can still find your next step here:</p><Link to="/goals?view=timetable">Organize my study time ↗</Link><Link to="/learning?view=skills">Understand my skill gaps ↗</Link><Link to="/interviews">Practice for an interview ↗</Link><button className="text-button" onClick={status.reload}>Check connection again</button></section>
  return <section className="panel coach-panel"><div className="section-title"><div><span className="eyebrow">CAREER GUIDANCE</span><h2>Ask CareerForge</h2></div><span className="pill">{status.loading ? 'Connecting…' : status.data.available ? 'AI coach' : 'Profile-aware guide'}</span></div><p>Ask about recruiter calls, interviews, projects, or what to focus on next.</p>
    <div className="coach-prompts">{['Plan my week', 'Suggest a portfolio project', 'Help me prepare for interviews'].map(prompt => <button type="button" key={prompt} onClick={() => setQuestion(prompt)}>{prompt}<span>↗</span></button>)}</div>
    <ErrorNotice message={error || status.error} retry={status.error ? status.reload : undefined}/>
    <form onSubmit={ask}><label className="sr-only" htmlFor="coach-question">Your career question</label><textarea id="coach-question" maxLength={1000} rows="2" required value={question} onChange={e => setQuestion(e.target.value)} placeholder="What should I focus on next?"/>
    {status.data.available && <label className="consent-check"><input type="checkbox" checked={consent} onChange={e => setConsent(e.target.checked)}/>Share my question, target role, study hours, and skill summary with OpenAI. My resume and account details are not included automatically.</label>}
    <div className="coach-form-footer"><small>{status.data.available ? 'Keep personal details out of your question.' : 'Uses your saved role, skills, learning plan, and progress.'}</small><button className="primary" disabled={busy || status.loading || Boolean(status.error) || (status.data.available && !consent)}>{busy ? 'Thinking…' : 'Get guidance'}<Icon name="arrow" size={16}/></button></div></form>
    {reply && <div className="coach-answer" role="status"><span className="eyebrow">{reply.mode === 'AI' ? 'AI-GENERATED GUIDANCE' : 'YOUR CAREERFORGE RESPONSE'}</span><div>{reply.answer}</div><small>{reply.notice}</small></div>}
  </section>
}

export function JobComparison() {
  return <Link className="job-match-entry" to="/job-match"><span className="icon-tile peach"><Icon name="search"/></span><div><h3>Tailor your resume before you apply</h3><p>Compare skills, qualifications, responsibilities, and important language without claiming experience you do not have.</p></div><span className="text-link">Open Job Match Analyzer</span><Icon name="arrow" size={17}/></Link>
}

export function ResumeEvidence({ resource }) {
  return <section className="panel resume-evidence"><div className="section-title"><div><span className="eyebrow">SEE WHAT THE SYSTEM FOUND</span><h2>Resume evidence</h2></div><span className="pill">Local text analysis</span></div><p>Review the excerpts before trusting an extracted skill. A mention is not proof of proficiency. Correct your toolkit below if a match is wrong.</p><ErrorNotice message={resource.error} retry={resource.reload}/>{resource.loading ? <Loading/> : resource.data.length ? resource.data.map((resume,index) => <details key={resume.id} open={index === 0}><summary>{resume.fileName}<span>{resume.evidence.length} skill mentions</span></summary>{resume.evidence.length ? <div className="evidence-grid">{resume.evidence.map(item => <div key={item.skill}><strong>{item.skill}</strong><blockquote>“{item.excerpt}”</blockquote></div>)}</div> : <p>No supported technical skill mentions were found. You can add skills manually.</p>}</details>) : !resource.error && <div className="evidence-placeholder"><Icon name="upload"/><p>Upload a resume to see the text behind each detected skill.</p></div>}</section>
}
