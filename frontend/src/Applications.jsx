import { JobComparison } from './JourneyWidgets'
import { useResource } from './useResource'
import { useState } from 'react'
import { api, userPath } from './api'
import { Icon, PageHeading, ErrorNotice, Empty, Loading } from './ui'
const statuses = ['Applied', 'Interview', 'Selected', 'Rejected']
function safeUrl(value) { try { const url = new URL(value); return ['https:', 'http:'].includes(url.protocol) ? url.href : null } catch { return null } }
export default function Applications() {
  const { data, loading, error, reload, setData } = useResource(userPath('applications'))
  const [showForm, setShowForm] = useState(false)
  const [saving, setSaving] = useState(false)
  const [pending, setPending] = useState([])
  const [feedback, setFeedback] = useState('')
  const [query, setQuery] = useState('')
  const [filter, setFilter] = useState('All')
  async function submit(event) {
    event.preventDefault()
    if (saving) return
    const { status, ...job } = Object.fromEntries(new FormData(event.currentTarget))
    for (const key of Object.keys(job)) job[key] = job[key].trim()
    if (!job.title || !job.company) { setFeedback('Enter a job title and company.'); return }
    if (job.applicationUrl && !safeUrl(job.applicationUrl)) { setFeedback('Enter a valid HTTP or HTTPS job URL.'); return }
    setSaving(true); setFeedback('')
    try {
      const application = await api('/applications', { method: 'POST', body: JSON.stringify({ job, status }) })
      setData(previous => [application, ...previous]); setShowForm(false)
    } catch (err) { setFeedback(err.message) } finally { setSaving(false) }
  }
  async function update(application, status) {
    setPending(previous => [...previous, application.id]); setFeedback('')
    try {
      const saved = await api(`/applications/${application.id}`, { method: 'PUT', body: JSON.stringify({ status }) })
      setData(previous => previous.map(item => item.id === application.id ? saved : item))
    } catch (err) { setFeedback(err.message) } finally { setPending(previous => previous.filter(id => id !== application.id)) }
  }
  const filtered = data.filter(item => (filter === 'All' || item.status === filter) && `${item.job?.title} ${item.job?.company}`.toLowerCase().includes(query.toLowerCase()))
  return <><PageHeading eyebrow="OPPORTUNITIES, ORGANIZED" title="Applications" description="From first application to your next offer. Keep every opportunity in view."><button className="primary" onClick={() => setShowForm(!showForm)}>{showForm ? 'Close form' : <><Icon name="plus"/> Add application</>}</button></PageHeading><JobComparison/><div className="pipeline">{statuses.map(status => <button key={status} className={filter === status ? 'selected' : ''} onClick={() => setFilter(filter === status ? 'All' : status)} aria-pressed={filter === status}><span className={`status-dot ${status.toLowerCase()}`}/><span>{status}</span><strong>{data.filter(a => a.status === status).length}</strong></button>)}</div><ErrorNotice message={error} retry={reload}/><ErrorNotice message={feedback}/>{showForm && <section className="panel form-panel"><h2>A new opportunity</h2><p>Add the details. You can update its status as things move forward.</p><form onSubmit={submit}><div className="form-grid"><label>Job title<input name="title" autoFocus required maxLength={255} placeholder="e.g. Frontend developer"/></label><label>Company<input name="company" required maxLength={255} placeholder="Company name"/></label><label>Location<input name="location" maxLength={255} placeholder="e.g. Bengaluru or Remote"/></label><label>Employment type<select name="employmentType">{['Full-time', 'Part-time', 'Internship', 'Contract'].map(type => <option key={type}>{type}</option>)}</select></label><label>Status<select name="status">{statuses.map(status => <option key={status}>{status}</option>)}</select></label><label>Job link <span className="optional">(optional)</span><input name="applicationUrl" type="url" maxLength={255} placeholder="https://…"/></label></div><div className="form-actions"><button className="primary" disabled={saving}>{saving ? 'Saving…' : 'Save application'}</button><button className="secondary" type="button" disabled={saving} onClick={() => setShowForm(false)}>Cancel</button></div></form></section>}<div className="section-title"><h2>All opportunities <span className="count">{data.length}</span></h2><div className="search-field"><Icon name="search" size={18}/><input aria-label="Search applications" value={query} onChange={e => setQuery(e.target.value)} placeholder="Search company or role…"/></div></div>{filter !== 'All' && <button className="text-button filter-reset" onClick={() => setFilter('All')}>Showing {filter} · Clear filter ×</button>}{loading ? <Loading/> : !error && (filtered.length ? <div className="application-grid">{filtered.map(item => <article className="panel application" key={item.id}><div className="section-title"><span className="company-avatar">{(item.job?.company || 'C').slice(0, 2).toUpperCase()}</span><span className={`pill ${item.status?.toLowerCase()}`}>{item.status}</span></div><h3>{item.job?.title || 'Untitled role'}</h3><p className="company-name">{item.job?.company}</p><p className="job-meta">{item.job?.location || 'Location not specified'} <span>·</span> {item.job?.employmentType}</p><div className="application-footer"><label>Status<select aria-label={`Status for ${item.job?.title} at ${item.job?.company}`} value={item.status} disabled={pending.includes(item.id)} onChange={e => update(item, e.target.value)}>{statuses.map(status => <option key={status}>{status}</option>)}</select></label>{safeUrl(item.job?.applicationUrl) && <a className="text-link" href={safeUrl(item.job.applicationUrl)} target="_blank" rel="noopener noreferrer">View job ↗</a>}</div></article>)}</div> : <Empty icon="briefcase" title={data.length ? 'No matching opportunities' : 'Your next opportunity belongs here'} description={data.length ? 'Try a different search or clear your filters.' : 'Add an application to keep track of every step, from applied to selected.'}><button className="primary" onClick={() => { if (data.length) { setQuery(''); setFilter('All') } else setShowForm(true) }}>{data.length ? 'Clear filters' : 'Add your first application'}</button></Empty>)}</>
}


