import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api, userKey } from './api'
import { useResource } from './useResource'
import { Empty, ErrorNotice, Icon, Loading, PageHeading } from './ui'
import './JobMatch.css'
import SemanticReview from './SemanticReview'

const statusLabel = {
  MATCHED: 'Shown in this resume',
  PROFILE_ONLY: 'In your profile, weak in resume',
  MISSING: 'No supporting evidence found',
  CLEARLY_REFLECTED: 'Clearly reflected',
  PARTLY_REFLECTED: 'Partly reflected',
  NOT_CLEAR: 'Not clear in resume',
}

function SkillGroup({ title, description, items, tone }) {
  return <section className={`match-skill-group ${tone}`}><div><span className="eyebrow">{title}</span><p>{description}</p></div>
    <div className="match-skill-list">{items.length ? items.map(item => <details key={item.skill}><summary><strong>{item.skill}</strong><span>{statusLabel[item.status]}</span></summary><p>{item.guidance}</p>{item.evidence && <blockquote>“{item.evidence}”</blockquote>}{item.profileLevel > 0 && <small>CareerForge self-rating: {item.profileLevel}%</small>}</details>) : <p className="match-none">Nothing in this group.</p>}</div>
  </section>
}

function Results({ analysis }) {
  const result = analysis.result
  const matched = result.skills.filter(item => item.status === 'MATCHED')
  const profileOnly = result.skills.filter(item => item.status === 'PROFILE_ONLY')
  const missing = result.skills.filter(item => item.status === 'MISSING')
  const missingKeywords = result.keywords.filter(item => item.status === 'MISSING')
  return <div className="match-results" aria-live="polite">
    <section className="panel match-next-actions"><h2>Your next steps</h2><p>{profileOnly.length ? 'Start by adding evidence for ' + profileOnly.slice(0,3).map(item => item.skill).join(', ') + ' — skills already in your profile.' : missing.length ? 'Build evidence for ' + missing.slice(0,3).map(item => item.skill).join(', ') + ' before claiming them.' : 'Review the requirements and make your strongest project evidence easy to find.'}</p><Link className="text-link" to="/learning">Continue with Skills & Learning <Icon name="arrow" size={16}/></Link></section>    <section className="match-score-card"><div className="match-score" style={{'--score': `${result.score * 3.6}deg`}}><div><strong>{result.score}<span>%</span></strong><small>KEYWORD ALIGNMENT</small></div></div><div><span className="eyebrow">ANALYSIS FOR</span><h2>{analysis.jobTitle}</h2><p>{result.scoreLabel}. Compared with <strong>{analysis.resumeLabel}</strong>.</p></div></section>
    <div className="integrity-note"><Icon name="check"/><p><strong>Keep every claim defensible.</strong> {result.integrityNote}</p></div>


    <div className="match-skill-board">

      {profileOnly.length > 0 && <SkillGroup title="UNDERREPRESENTED SKILLS" description="Saved profile skills that need clearer, truthful evidence in this resume." items={profileOnly} tone="review"/>}
      {missing.length > 0 && <SkillGroup title="SKILL GAPS" description="Requirements without evidence in this resume or your saved profile." items={missing} tone="gap"/>}
    </div>

    <details className="matched-skills"><summary>Skills already reflected · {matched.length}</summary><SkillGroup title="MATCHING SKILLS" description="Terms already shown in your resume." items={matched} tone="good"/></details>
    <details className="panel match-keywords"><summary>Keywords to review · {missingKeywords.length}</summary><div className="section-title"><div><span className="eyebrow">IMPORTANT LANGUAGE</span><h2>Missing or underrepresented keywords</h2><p>Use these only when they accurately describe your work, education, or projects.</p></div><span className="pill">{missingKeywords.length} to review</span></div><div className="keyword-list">{missingKeywords.length ? missingKeywords.map(item => <span key={item.keyword}>{item.keyword}</span>) : <p>The resume reflects the important supported terms detected in this description.</p>}</div></details>

    <details className="panel requirement-panel"><summary>Qualifications & responsibilities · view details</summary><div className="section-title"><div><span className="eyebrow">READ THE ROLE CAREFULLY</span><h2>Qualifications & responsibilities</h2></div></div>{result.requirements.length ? <div className="requirement-list">{result.requirements.map((item,index) => <article key={`${item.type}-${index}`}><div><span>{item.type === 'QUALIFICATION' ? 'Qualification' : 'Responsibility'}</span><strong className={item.status.toLowerCase().replace('_','-')}>{statusLabel[item.status]}</strong></div><p>{item.text}</p>{item.reflectedTerms.length > 0 && <small>Reflected terms: {item.reflectedTerms.join(' · ')}</small>}</article>)}</div> : <p>No clearly structured qualification or responsibility statements were detected. Review the original description before applying.</p>}</details>

    <details className="panel suggestion-panel"><summary>Resume editing suggestions · {result.suggestions.length}</summary><div className="section-title"><div><span className="eyebrow">EDIT WITH EVIDENCE</span><h2>Resume suggestions</h2><p>Templates contain placeholders so CareerForge does not invent achievements or numbers.</p></div></div><div className="suggestion-list">{result.suggestions.map((item,index) => <article key={`${item.title}-${index}`}><span>{String(index + 1).padStart(2,'0')}</span><div><h3>{item.title}</h3><p>{item.guidance}</p><div className="rewrite-template"><small>TRUTHFUL BULLET TEMPLATE</small>{item.example}</div><em>{item.truthCheck}</em></div></article>)}</div></details>

    <details className="match-advanced"><summary>View detailed evidence & meaning comparison</summary>    <details className="panel"><summary>How this score is calculated</summary><small>{result.method}</small><p>Skills carry 55 parts, keywords 15, qualifications 15, and responsibilities 15. Empty categories are excluded and remaining weights are normalized. Resume skill mentions receive full credit; profile-only claims receive 55% credit. Requirements use token coverage, not verification of a degree or experience. The score is not an ATS score or a hiring probability.</p></details>
    {result.concepts?.evidence.length > 0 && <section className="panel"><h2>Related experience</h2><p>{result.concepts.supportedConcepts} of {result.concepts.requiredConcepts} recognized concepts have related wording.</p><div className="requirement-list">{result.concepts.evidence.map(item => <article key={item.concept}><strong>{item.concept}</strong><p>Job: {item.requirement}</p><p>{item.resumeEvidence ? 'Resume: ' + item.resumeEvidence : 'No supporting passage found. This does not prove you lack the skill.'}</p></article>)}</div><small>{result.concepts.limitation}</small></section>}
    <SemanticReview key={analysis.id} analysis={analysis}/>
</details>
    <section className="panel match-learning"><div><span className="eyebrow">CLOSE REAL GAPS</span><h2>Learning recommendations</h2><p>Learn missing skills before adding them as resume claims. Each recommendation uses CareerForge’s reviewed resource and applied task.</p><div className="heading-actions"><Link className="primary" to="/learning">Open my learning plan <Icon name="arrow"/></Link><Link className="secondary" to="/skills">Review skill gaps</Link></div></div>{result.learningRecommendations.length ? <div className="match-learning-list">{result.learningRecommendations.map(item => <article key={item.skill}><span>{item.hours}h practice</span><h3>{item.skill}</h3><p>{item.practice}</p><a className="text-link" href={item.url} target="_blank" rel="noopener noreferrer">Learn with {item.provider} ↗</a></article>)}</div> : <div className="learning-clear"><Icon name="check" size={28}/><strong>No resource suggestions for this analysis</strong><p>The catalog may not cover this job’s requirements. Review the description and your evidence before applying.</p></div>}</section>
  </div>
}

export default function JobMatch() {
  const history = useResource('/job-matches')
  const [draft,setDraft] = useState(() => { try { return JSON.parse(sessionStorage.getItem(userKey('job-match-draft'))) || {} } catch { return {} } })
  const [draftWarning,setDraftWarning] = useState('')
  function remember(event) {
    const next = { ...draft, [event.target.name]: event.target.value }
    setDraft(next)
    try { sessionStorage.setItem(userKey('job-match-draft'), JSON.stringify(next)); setDraftWarning('') }
    catch { setDraftWarning('Draft storage is unavailable. Keep this page open until your analysis is saved.') }
  }

  const [mode,setMode] = useState(() => ['upload','paste','saved'].includes(draft.resumeMode) ? draft.resumeMode : 'upload')
  const [file,setFile] = useState(null)
  const [analysis,setAnalysis] = useState(null)
  const [busy,setBusy] = useState(false)
  const [loadingId,setLoadingId] = useState(null)
  const [removing,setRemoving] = useState(null)
  const [error,setError] = useState('')

  async function analyze(event) {
    event.preventDefault()
    if (busy) return
    const values = Object.fromEntries(new FormData(event.currentTarget))
    if (mode === 'upload' && (!file || !/\.(pdf|docx)$/i.test(file.name) || file.size > 10*1024*1024 || !file.size)) { setError('Choose a non-empty PDF or DOCX resume under 10 MB.'); return }
    if (mode === 'paste' && !values.resumeText?.trim()) { setError('Paste your resume text.'); return }
    setBusy(true);setError('')
    try {
      let next
      if (mode === 'upload') {
        const body = new FormData();body.append('jobTitle',values.jobTitle || '');body.append('jobDescription',values.jobDescription);body.append('resume',file)
        next = await api('/job-matches/analyze-file',{method:'POST',body})
      } else {
        next = await api('/job-matches/analyze',{method:'POST',body:JSON.stringify({jobTitle:values.jobTitle,jobDescription:values.jobDescription,resumeText:mode === 'paste' ? values.resumeText : '',useSavedResume:mode === 'saved'})})
      }
      setAnalysis(next);history.reload();setTimeout(() => document.querySelector('.match-results')?.scrollIntoView({behavior:'smooth',block:'start'}),50)
    } catch (err) { setError(err.message) } finally { setBusy(false) }
  }
  async function openSaved(id) {
    setLoadingId(id);setError('')
    try { setAnalysis(await api(`/job-matches/${id}`));setTimeout(() => document.querySelector('.match-results')?.scrollIntoView({behavior:'smooth',block:'start'}),50) }
    catch (err) { setError(err.message) } finally { setLoadingId(null) }
  }
  async function removeSaved(id) {
    setRemoving(id);setError('')
    try { await api(`/job-matches/${id}`,{method:'DELETE'});history.setData(items => items.filter(item => item.id !== id));if (analysis?.id === id) setAnalysis(null) }
    catch (err) { setError(err.message) } finally { setRemoving(null) }
  }
  return <>
    <PageHeading eyebrow="RESUME COMPARISON" title="Job Match" description="Bring a job description and your resume. Get a clear list of what to strengthen before you apply."/>
    <section className="match-workbench">
      <form className="panel match-form" onSubmit={analyze}>
        <div className="match-form-heading"><span className="icon-tile peach"><Icon name="search"/></span><div><h2>Compare your resume</h2><p>Two inputs. Clear next steps.</p></div></div>
        <label>Job title <span className="optional">(optional)</span><input name="jobTitle" value={draft.jobTitle || ''} onChange={remember} maxLength="160" placeholder="e.g. Junior Data Analyst"/></label>
        <label>1. Paste the job description<textarea name="jobDescription" value={draft.jobDescription || ''} onChange={remember} minLength="80" maxLength="30000" rows="7" required placeholder="Paste the complete responsibilities, qualifications, technologies, and requirements…"/></label>
        <fieldset><legend>2. Choose your resume</legend><div className="resume-mode">{[['upload','Upload file','PDF or DOCX · up to 10 MB'],['paste','Paste text','Copy from your resume'],['saved','Saved resume','Use your latest upload']].map(([value,title,copy]) => <label className={mode === value ? 'selected' : ''} key={value}><input type="radio" name="resumeMode" value={value} checked={mode === value} onChange={() => {setMode(value);remember({target:{name:'resumeMode',value}});setError('')}}/><span><strong>{title}</strong><small>{copy}</small></span></label>)}</div></fieldset>
        {mode === 'upload' && <label>Resume file<input type="file" accept=".pdf,.docx" onChange={event => setFile(event.target.files?.[0] || null)}/></label>}
        {mode === 'paste' && <label>Resume text<textarea name="resumeText" value={draft.resumeText || ''} onChange={remember} maxLength="60000" rows="10" placeholder="Paste the resume exactly as you plan to submit it…"/></label>}
        <ErrorNotice message={error}/><ErrorNotice message={draftWarning}/>
        <button className="primary match-submit" disabled={busy}>{busy ? 'Analyzing requirements and evidence…' : 'Analyze this job'}<Icon name="arrow"/></button>
        <p className="draft-note">Your text draft stays in this tab. You can come back before closing it.</p>
        <details className="match-storage"><summary>How your information is saved</summary>        <small className="match-privacy">Unfinished text is remembered for this account in this browser tab until the tab closes. Files must be selected again. CareerForge stores the job description and analysis so you can reopen it. Up to 80 resume passages are saved with the analysis for semantic review. Uploaded files are read for text and are not saved as files by this feature. Delete an analysis to remove its saved passages.</small></details>
      </form>

      <aside className="panel saved-matches"><div className="section-title"><div><span className="eyebrow">REUSE THE JOB LATER</span><h2>Saved analyses</h2><p>The original job description is retained for future interview preparation.</p></div></div><ErrorNotice message={history.error} retry={history.reload}/>{history.loading ? <Loading/> : history.data.length ? <div className="saved-match-list">{history.data.map(item => <article key={item.id}><button onClick={() => openSaved(item.id)} disabled={Boolean(loadingId)}><span className="saved-score">{item.score}%</span><span><strong>{item.jobTitle}</strong><small>{item.resumeLabel} · {new Date(item.updatedAt).toLocaleDateString(undefined,{day:'numeric',month:'short',year:'numeric'})}</small></span></button><button className="text-button danger" onClick={() => removeSaved(item.id)} disabled={Boolean(removing)} aria-label={`Delete analysis for ${item.jobTitle}`}>{removing === item.id ? 'Deleting…' : 'Delete'}</button></article>)}</div> : <Empty icon="search" title="No saved matches yet" description="Your first completed comparison will appear here."/>}</aside>
    </section>
    {analysis && <Results analysis={analysis}/>}
  </>
}
