import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api, readSaved, userKey } from './api'
import { useResource } from './useResource'
import InterviewFeedback from './InterviewFeedback.jsx'
import { PageHeading, ErrorNotice, Loading, Empty } from './ui'

function Practice({ initial, onSaved }) {
  const [session, setSession] = useState(initial)
  const [index, setIndex] = useState(initial.currentQuestion)
  const [drafts, setDrafts] = useState(() => readSaved(`interview:${initial.id}`, {}))
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const question = session.questions[index]
  const answer = drafts[index] ?? session.answers[index]
  const mcq = Boolean(question.options?.length)
  const complete = session.status === 'COMPLETED'
  const answered = session.answers.filter(a => a.trim()).length
  function draft(value) {
    const next = { ...drafts, [index]: value }; setDrafts(next); setMessage('')
    try { localStorage.setItem(userKey(`interview:${session.id}`), JSON.stringify(next)) } catch { setMessage('Device draft storage is unavailable. Save your answer to keep it in your account.') }
  }
  async function save(finish = false) {
    setBusy(true); setError(''); setMessage('')
    try {
      const updated = await api(`/interviews/${session.id}/${finish ? 'finish' : 'answer'}`, { method: finish ? 'POST' : 'PUT', body: JSON.stringify(finish ? { version: session.version } : { index, answer, version: session.version }) })
      setSession(updated); onSaved()
      if (!finish) {
        const next = { ...drafts }; delete next[index]; setDrafts(next)
        try { localStorage.setItem(userKey(`interview:${session.id}`), JSON.stringify(next)) } catch { /* The answer is already saved on the server. */ }
        setIndex(updated.currentQuestion); setMessage('Answer saved to your account.')
      }
    } catch (err) { setError(err.message) } finally { setBusy(false) }
  }
  async function reload() {
    setBusy(true)
    try { setSession(await api(`/interviews/${session.id}`)); setError(''); setMessage('Saved session refreshed. Your device drafts are preserved.') } catch (err) { setError(err.message) } finally { setBusy(false) }
  }
  return <section className="panel interview-practice"><div className="section-title"><div><span className="eyebrow">{complete ? 'SESSION COMPLETE' : 'YOUR PRACTICE ROOM'}</span><h2>{session.role}</h2></div><span className="pill mint">{answered}/{session.questions.length} answers saved</span></div><progress value={answered} max={session.questions.length} aria-label="Interview answers saved"/><ErrorNotice message={error} retry={reload}/>{message && <p role="status" className="notice success">{message}</p>}{complete ? <>{session.score != null && <h2>Your score: {session.score} / {session.questions.length}</h2>}<InterviewFeedback session={session}/>{session.questions.map((q, i) => <details className="interview-review" key={q.prompt} open={i === 0}><summary>{i + 1}. {q.topic}</summary><h3>{q.prompt}</h3><p className="interview-answer">Your answer: {q.options?.length ? q.options[Number(session.answers[i])] : session.answers[i]}</p>{q.options?.length ? <><strong>{Number(session.answers[i]) === q.correctIndex ? 'Correct' : 'Incorrect'} · Answer: {q.options[q.correctIndex]}</strong><h4>How to approach it</h4><p>{q.explanation}</p></> : <><strong>Review your approach</strong><ul>{q.reviewPoints.map(point => <li key={point}>{point}</li>)}</ul></>}</details>)}</> : <><div className="interview-tabs" aria-label="Interview questions">{session.questions.map((q, i) => <button className={index === i ? 'primary' : 'secondary'} key={q.prompt} disabled={busy} onClick={() => setIndex(i)} aria-pressed={index === i}> {i + 1}{session.answers[i] ? ' ✓' : ''}<span className="sr-only"> {q.topic}</span></button>)}</div><p className="muted">Work through one question at a time. Save before leaving to resume on another device. Answers and explanations become available after you finish the interview.</p><span className="eyebrow">QUESTION {index + 1} OF {session.questions.length} · {question.topic}</span><h2 className="interview-question">{question.prompt}</h2><form onSubmit={event => { event.preventDefault(); save() }}>{mcq ? <fieldset className="mcq-options"><legend>Choose one answer</legend>{question.options.map((option,i) => <label key={option}><input type="radio" name="mcq-answer" value={i} checked={answer === String(i)} onChange={() => draft(String(i))}/><span>{String.fromCharCode(65+i)}. {option}</span></label>)}</fieldset> : <label htmlFor="interview-answer">Your answer<textarea id="interview-answer" rows={9} maxLength={5000} required value={answer} onChange={event => draft(event.target.value)} placeholder="Explain your approach, give a concrete example, and discuss tradeoffs…"/></label>}{!mcq && <p className="muted">{answer.length}/5,000 characters · Drafts stay on this device. Save an answer to sync it to your account.</p>}<div className="form-actions"><button className="primary" disabled={busy || !answer.trim()}>{busy ? 'Saving…' : index === session.questions.length - 1 ? 'Save answer' : 'Save & continue'}</button><button type="button" className="secondary" disabled={busy || answered !== session.questions.length || Object.keys(drafts).some(key => drafts[key] !== session.answers[key])} onClick={() => save(true)}>Finish interview</button></div></form>{complete && session.answers[index] && <details className="interview-review"><summary>Review points for this question</summary><ul>{question.reviewPoints.map(point => <li key={point}>{point}</li>)}</ul></details>}</>}<p className="muted interview-disclaimer">MCQs are scored after submission; written interviews use self-review. Questions cover common concepts, not a verified list of company interview questions. This is practice, not a hiring assessment.</p></section>
}
function SessionLoader({ id, onSaved }) {
  const resource = useResource(`/interviews/${id}`)
  return resource.loading ? <Loading/> : resource.error ? <ErrorNotice message={resource.error} retry={resource.reload}/> : <Practice key={resource.data.id} initial={resource.data} onSaved={onSaved}/>
}
export default function Interviews() {
  const aiStatus = useResource('/journey/coach')
  const [useAI,setUseAI] = useState(false)
  const journey = useResource('/journey')
  const history = useResource('/interviews')
  const [params, setParams] = useSearchParams()
  const [role, setRole] = useState('')
  const [questionCount, setQuestionCount] = useState(5)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const id = params.get('session')
  async function start(event) {
    event.preventDefault(); setBusy(true); setError('')
    try { const session = await api('/interviews', { method: 'POST', body: JSON.stringify({ role: role || journey.data.role, questionCount, useAI: questionCount === 30 && useAI }) }); history.reload(); setParams({ session: session.id }) } catch (err) { setError(err.message) } finally { setBusy(false) }
  }
  const resume = history.data.find?.(session => session.status === 'IN_PROGRESS')
  return <><PageHeading eyebrow="TURN KNOWLEDGE INTO CONFIDENCE" title="Mock interviews" description="Practice for the role you want. Explain your thinking, reflect, and come back stronger."/><ErrorNotice message={error}/><ErrorNotice message={journey.error} retry={journey.reload}/>{id && <><Link className="text-link" to="/interviews">← All interviews</Link><SessionLoader key={id} id={id} onSaved={history.reload}/></>}{!id && <>{resume && <section className="resume-practice"><div><h2>Continue your {resume.role} practice</h2><p>{resume.answered}/{resume.total} answers saved</p></div><button className="primary" onClick={() => setParams({session:resume.id})}>Resume practice</button></section>}<section className="panel interview-intro"><div><span className="eyebrow">A SAFE SPACE TO PRACTICE</span><h2>Your next interview starts here.</h2><p>Start with five questions for your target role. Save your answers and return whenever you need. Choose a longer session when you have more time.</p></div><form onSubmit={start}><label>Practice role<select value={role || journey.data.role || ''} onChange={event => setRole(event.target.value)} disabled={journey.loading}>{(journey.data.roles || []).map(item => <option key={item}>{item}</option>)}</select></label><label>Session length<select value={questionCount} onChange={event => setQuestionCount(Number(event.target.value))} disabled={busy}><option value={30}>Technical + aptitude · 30 MCQs</option><option value={5}>Quick practice · 5 questions</option><option value={10}>Standard practice · 10 questions</option><option value={20}>Full preparation · 20 questions</option></select></label><p className="muted">{questionCount === 30 ? "Save all 30 answers, then finish to see your score, correct answers, and explanations." : questionCount === 5 ? "A short personalized drill. Review your answers afterward and practise regularly." : questionCount === 10 ? "A broader practice session. Aim to explain each answer aloud." : "More technical, project, and problem-solving practice. You can save and resume later."}</p><p className="muted">{questionCount === 30 ? "MCQs use the selected role and aptitude question bank." : "Written sessions include questions about role skills mentioned in your latest resume and an area below your saved target rating. These are practice cues, not verified expertise."}</p><>{questionCount === 30 && aiStatus.data.available && <label className="consent-check"><input type="checkbox" checked={useAI} onChange={e => setUseAI(e.target.checked)}/>Generate with AI. Send only my selected role to OpenAI. Generated answers may contain mistakes.</label>}</><button className="primary" disabled={busy || journey.loading || Boolean(journey.error)}>{busy ? 'Preparing…' : 'Start a new interview'}</button></form></section></>}<div className="section-title"><h2>Previous sessions</h2><span className="muted">Recent 30 sessions</span></div><ErrorNotice message={history.error} retry={history.reload}/>{history.loading ? <Loading/> : !history.error && (history.data.length ? <div className="interview-history">{history.data.map(session => <article className="panel" key={session.id}><span className={`pill ${session.status === 'COMPLETED' ? 'mint' : ''}`}>{session.status === 'COMPLETED' ? 'Completed' : 'In progress'}</span><h3>{session.role}</h3><p>{session.answered}/{session.total} answers saved · {new Date(session.startedAt).toLocaleDateString()}</p><button className="secondary" onClick={() => setParams({ session: session.id })}>{session.status === 'COMPLETED' ? 'Review answers' : 'Resume interview'}</button></article>)}</div> : <Empty icon="briefcase" title="Build confidence, one answer at a time" description="Choose your desired role above to start your first practice session."/>)}</>
}
