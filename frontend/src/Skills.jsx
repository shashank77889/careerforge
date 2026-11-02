import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api, userPath } from './api'
import { useResource } from './useResource'
import { Icon, PageHeading, ErrorNotice, Empty, Loading } from './ui'
import { JourneySettings, ReadinessDial, SkillMap } from './JourneyWidgets'

export default function Skills({ embedded = false }) {
  const skills = useResource(userPath('skills'))
  const journey = useResource('/journey')
  const [busy, setBusy] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [file, setFile] = useState(null)
  const [remove, setRemove] = useState(null)
  async function action(key, work) {
    if (busy) return
    setBusy(key); setError(''); setSuccess('')
    try { await work() } catch (err) { setError(err.message) } finally { setBusy('') }
  }
  function add(event) {
    event.preventDefault()
    const form = event.currentTarget
    const values = Object.fromEntries(new FormData(form))
    if (!values.name.trim()) {setError('Enter a skill name.');return}
    action('add',async () => {
      const skill = await api(userPath('skills'),{method:'POST',body:JSON.stringify({name:values.name.trim(),proficiency:Number(values.proficiency),confidence:0,targetLevel:80})})
      skills.setData(previous => [...previous,skill]); journey.reload(); form.reset(); setSuccess('Skill added. Your readiness comparison has been refreshed.')
    })
  }
  function upload(event) {
    event.preventDefault()
    if (!file || !/\.(pdf|docx)$/i.test(file.name) || file.size > 10*1024*1024 || !file.size) {setError('Choose a non-empty PDF or DOCX resume under 10 MB.');return}
    const form = event.currentTarget
    action('upload',async () => {
      const body = new FormData();body.append('file',file)
      await api(userPath('resumes'),{method:'POST',body});skills.reload();journey.reload();setFile(null);form.reset();setSuccess('Skills extracted. Review the list and rate your proficiency below.')
    })
  }
  function update(skill,value) {
    action(`skill-${skill.id}`,async () => {
      const updated = await api(`/skills/${skill.id}/user/${localStorage.getItem('userId')}`,{method:'PUT',body:JSON.stringify({proficiency:Number(value)})})
      skills.setData(previous => previous.map(item => item.id === skill.id ? updated : item));journey.reload();setSuccess(`${skill.name} proficiency saved. This is your self-rating, not an assessment result.`)
    })
  }
  const data = journey.data
  return <>
    {!embedded && <PageHeading eyebrow="PREPARATION" title="Resume & skill gaps" description="Review your resume evidence and rate your skills."/>}
    <ErrorNotice message={error}/>{success && <div className="notice success" role="status">{success}</div>}
    {data.readiness && <JourneySettings journey={data} onSave={journey.setData}/>}<ErrorNotice message={journey.error} retry={journey.reload}/>
    <section className="resume-panel"><span className="icon-tile mint"><Icon name="upload" size={26}/></span><div><span className="eyebrow">START WITH WHAT YOU ALREADY HAVE</span><h2>Your resume is the starting point.</h2><p>Extract technical skills from your resume, then review and rate them below.</p><form onSubmit={upload} className="upload-form"><input type="file" aria-label="Choose resume" accept=".pdf,.docx" onChange={e => setFile(e.target.files?.[0] || null)} disabled={Boolean(busy)}/><button className="primary" disabled={Boolean(busy) || !file}>{busy === 'upload' ? 'Analyzing resume…' : 'Analyze resume'}</button></form><small>PDF or DOCX · Up to 10 MB · Text-based resumes · Processed locally on CareerForge</small></div></section>
    <section className="panel"><div className="section-title"><div><span className="eyebrow">YOUR TOOLKIT</span><h2>Skills you bring <span className="count">{skills.data.length}</span></h2><p>Rate what you can do independently. Resume mentions start at 0% until you review them.</p></div></div><ErrorNotice message={skills.error} retry={skills.reload}/>
      <form className="add-skill-form" onSubmit={add}><label>Skill name<input name="name" required maxLength={100} placeholder="e.g. Python"/></label><label>Proficiency<select name="proficiency" defaultValue="40">{[[0,'0% · Not rated'],[20,'20% · Getting started'],[40,'40% · Familiar'],[60,'60% · Comfortable'],[80,'80% · Proficient'],[100,'100% · Advanced']].map(([value,label]) => <option key={value} value={value}>{label}</option>)}</select></label><button className="primary" disabled={Boolean(busy)}><Icon name="plus"/>{busy === 'add' ? 'Adding…' : 'Add skill'}</button></form>
      {skills.loading ? <Loading/> : !skills.error && (skills.data.length ? <div className="skill-list">{skills.data.map(skill => <div className="skill-row" key={skill.id}><span className="skill-initial">{skill.name?.slice(0,1)}</span><div><h3>{skill.name} <small className="source-tag">{skill.source === 'RESUME' ? 'Resume mention' : 'Self-reported'}</small></h3><progress max="100" value={skill.proficiency || 0} aria-label={`${skill.name} proficiency`}/></div><label className="skill-level"><span className="sr-only">{`Proficiency for ${skill.name}`}</span><select value={skill.proficiency || 0} disabled={Boolean(busy)} onChange={event => update(skill,event.target.value)}>{[...new Set([0,20,40,60,80,100,skill.proficiency || 0])].sort((a,b) => a-b).map(value => <option key={value} value={value}>{value}%</option>)}</select></label>{remove === skill.id ? <div className="delete-confirm"><span>Remove?</span><button className="text-button danger" disabled={Boolean(busy)} onClick={() => action('delete',async () => {await api(`/skills/${skill.id}/user/${localStorage.getItem('userId')}`,{method:'DELETE'});skills.setData(previous => previous.filter(item => item.id !== skill.id));journey.reload();setRemove(null);setSuccess('Skill removed from your toolkit.')})}>Yes</button><button className="text-button" onClick={() => setRemove(null)}>Cancel</button></div> : <button className="icon-button" aria-label={`Remove ${skill.name}`} disabled={Boolean(busy)} onClick={() => setRemove(skill.id)}>×</button>}</div>)}</div> : <Empty icon="chart" title="Give your strengths a place to grow" description="Add a skill above or upload a resume to get started."/>)}
    </section>
    {data.readiness && <><section className="readiness-summary panel"><ReadinessDial compact score={data.readiness.score}/><div><span className="eyebrow">YOUR PATH TO {data.role.toUpperCase()}</span><h2>A clearer picture of your starting point.</h2><p>{data.readiness.matched} targets met · {data.readiness.developing} skills developing · {data.readiness.missing} skills to add</p><div className="heading-actions"><button className="primary" disabled={Boolean(busy) || journey.loading} onClick={() => action('snapshot',async () => {journey.setData(await api('/journey/reassess',{method:'POST'}));setSuccess('Readiness snapshot saved. Compare it with your next reflection.')})}>{busy === 'snapshot' ? 'Saving…' : 'Save readiness snapshot'}</button><Link className="text-link" to="/learning">Build on these gaps ↗</Link></div></div></section><SkillMap readiness={data.readiness} plan={data.plan}/><section className="panel readiness-history"><div className="section-title"><h2>Your growth over time</h2><span className="pill">Self-reported snapshots</span></div>{data.history.length ? <div className="history-list">{data.history.map(snapshot => <div key={snapshot.id}><time>{new Date(snapshot.recordedAt).toLocaleDateString(undefined,{day:'numeric',month:'short'})}</time><progress value={snapshot.score} max="100" aria-label={`Readiness on ${new Date(snapshot.recordedAt).toLocaleString()}`}/><strong>{snapshot.score}%</strong></div>)}</div> : <p>Save your first snapshot. Update your ratings after practicing, then save another to reflect on your progress.</p>}</section></>}
  </>
}
