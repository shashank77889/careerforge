import { useEffect, useMemo, useState } from 'react'
import { api, userKey } from './api'
import { Icon, ErrorNotice } from './ui'
import './StudyPlanner.css'
import { calendarFile, calendarLink } from './studyCalendar'

const DAYS = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY']
const LABELS = {MONDAY:'Mon',TUESDAY:'Tue',WEDNESDAY:'Wed',THURSDAY:'Thu',FRIDAY:'Fri',SATURDAY:'Sat',SUNDAY:'Sun'}

function addMinutes(time, minutes) {
  const [hour, minute] = time.split(':').map(Number)
  const value = hour * 60 + minute + minutes
  const h = Math.floor(value / 60) % 24
  const m = value % 60
  return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}`
}

function readableTime(time) {
  const [hour, minute] = time.split(':').map(Number)
  return new Intl.DateTimeFormat(undefined,{hour:'numeric',minute:'2-digit'}).format(new Date(2026,0,1,hour,minute))
}

function buildSessions(journey, preferences) {
  const unfinished = journey.plan.filter(step => step.status !== 'COMPLETED')
  const totalMinutes = preferences.weeklyHours * 60
  const count = Math.ceil(totalMinutes / preferences.sessionMinutes)
  const defaultTopicLimit = Math.max(1,Math.min(3,Math.floor(count / 3)))
  const topics = preferences.focusSkills.length ? unfinished.filter(step => preferences.focusSkills.includes(step.skill)) : unfinished.slice(0,defaultTopicLimit)
  if (!topics.length || !preferences.days.length) return []
  const phases = ['Learn the essentials','Build without following along','Recall, explain, and review']
  const slots = Array.from({length:count},(_,index) => {
    const day = preferences.days[index % preferences.days.length]
    const daySlot = Math.floor(index / preferences.days.length)
    return {day,time:addMinutes(preferences.startTime,daySlot * (preferences.sessionMinutes + 15))}
  }).sort((a,b) => DAYS.indexOf(a.day) - DAYS.indexOf(b.day) || a.time.localeCompare(b.time))
  return slots.map((slot,index) => {
    const topic = topics[index % topics.length]
    const duration = Math.min(preferences.sessionMinutes, totalMinutes - index * preferences.sessionMinutes)
    const phase = phases[Math.floor(index / topics.length) % phases.length]
    const activity = phase.startsWith('Learn')
      ? `Use ${topic.resource.provider}: cover the relevant section, then write three points in your own words.`
      : phase.startsWith('Build') ? topic.resource.practice
      : `Close the resource. Explain ${topic.skill} from memory, check one weak point, and add a short reflection to the milestone.`
    return {...slot,duration,skill:topic.skill,phase,activity}
  })
}

function isoDate(date) {
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`
}

function currentWeekDate(day) {
  const now = new Date()
  const monday = new Date(now.getFullYear(),now.getMonth(),now.getDate())
  monday.setDate(monday.getDate() - ((now.getDay() + 6) % 7))
  const result = new Date(monday)
  result.setDate(monday.getDate() + DAYS.indexOf(day))
  return isoDate(result)
}

export default function StudyPlanner({ journey, onSaved }) {
  const saved = journey.studyPreferences || {}
  const available = journey.plan.filter(step => step.status !== 'COMPLETED')
  const [preferences,setPreferences] = useState({
    weeklyHours:journey.weeklyHours,
    days:saved.days?.length ? saved.days : ['MONDAY','WEDNESDAY','SATURDAY'],
    startTime:saved.startTime || '19:00',
    sessionMinutes:saved.sessionMinutes || 60,
    focusSkills:(saved.focusSkills || []).filter(skill => available.some(step => step.skill === skill))
  })
  const [activePreferences,setActivePreferences] = useState(preferences)
  const [selectedDay,setSelectedDay] = useState(DAYS[(new Date().getDay()+6)%7])
  const [calendarOpen,setCalendarOpen] = useState(false)
  const [editing,setEditing] = useState(false)
  const [busy,setBusy] = useState(false)
  const [message,setMessage] = useState('')
  const [error,setError] = useState('')
  const [logs,setLogs] = useState([])
  const [logsLoading,setLogsLoading] = useState(true)
  const [logging,setLogging] = useState(null)
  const [notifications,setNotifications] = useState(() => localStorage.getItem(userKey('study-reminders')) === 'on')
  const [now,setNow] = useState(() => new Date())
  const sessions = useMemo(() => buildSessions(journey,activePreferences),[journey,activePreferences])
  const datedSessions = useMemo(() => sessions.map((session,index) => ({...session,date:currentWeekDate(session.day),sessionKey:`${session.day}-${session.time}-${session.skill}-${index}`})),[sessions])
  const grouped = DAYS.map(day => ({day,sessions:datedSessions.filter(item => item.day === day)})).filter(group => group.sessions.length)
  const today = isoDate(now)
  const monday = currentWeekDate('MONDAY')
  const sunday = currentWeekDate('SUNDAY')

  useEffect(() => {
    const controller = new AbortController()
    api(`/journey/study-log?from=${monday}&to=${sunday}`,{signal:controller.signal}).then(setLogs).catch(err => { if (err.name !== 'AbortError') setError(err.message) }).finally(() => setLogsLoading(false))
    return () => controller.abort()
  },[journey.role,monday,sunday])

  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()),60000)
    return () => clearInterval(timer)
  },[])

  useEffect(() => {
    if (!notifications || typeof Notification === 'undefined' || Notification.permission !== 'granted') return
    const timers = datedSessions.filter(session => session.date === today && !logs.some(log => log.sessionKey === session.sessionKey && log.completed)).map(session => {
      const starts = new Date(`${session.date}T${session.time}:00`).getTime()
      const delay = starts - Date.now()
      if (delay <= 0 || delay > 2147483647) return null
      return setTimeout(() => (() => { try { new Notification('Time for '+session.skill,{body:session.duration+' minutes · '+session.phase}) } catch { /* Calendar reminders are available on unsupported browsers. */ } })(),delay)
    }).filter(Boolean)
    return () => timers.forEach(clearTimeout)
  },[notifications,datedSessions,logs,today])

  const studiedMinutes = logs.reduce((sum,entry) => sum + entry.studiedMinutes,0)
  const todaySessions = datedSessions.filter(session => session.date === today)
  const completedToday = todaySessions.filter(session => logs.some(log => log.sessionKey === session.sessionKey && log.completed))
  const overdueToday = todaySessions.filter(session => new Date(new Date(`${session.date}T${session.time}:00`).getTime() + session.duration * 60000) < now && !logs.some(log => log.sessionKey === session.sessionKey && log.completed))
  const nextSession = datedSessions.filter(session => new Date(`${session.date}T${session.time}:00`) >= now && !logs.some(log => log.sessionKey === session.sessionKey && log.completed)).sort((a,b) => new Date(`${a.date}T${a.time}:00`) - new Date(`${b.date}T${b.time}:00`))[0]
  const dayStatus = todaySessions.length && completedToday.length === todaySessions.length
    ? {kind:'done',title:"You’re done for today.",detail:'Your planned sessions are complete. Continue only if you have the energy; rest is part of a sustainable plan.'}
    : overdueToday.length ? {kind:'due',title:`${overdueToday.length} planned ${overdueToday.length === 1 ? 'session is' : 'sessions are'} still open today.`,detail:'Log the time you studied, or move the work by editing your timetable. The alert is a reminder, not a judgment.'}
    : nextSession?.date === today ? {kind:'next',title:`Next today: ${nextSession.skill} at ${readableTime(nextSession.time)}.`,detail:`${nextSession.duration} minutes for “${nextSession.phase}”.`}
    : nextSession ? {kind:'next',title:`Next session: ${LABELS[nextSession.day]} at ${readableTime(nextSession.time)}.`,detail:`${nextSession.skill} · ${nextSession.duration} minutes.`}
    : {kind:'next',title:'No more upcoming sessions this week.',detail:'Review earlier days for unfinished sessions, or adjust your schedule.'}

  function toggleDay(day) {
    setPreferences(previous => ({...previous,days:previous.days.includes(day) ? previous.days.filter(value => value !== day) : DAYS.filter(value => value === day || previous.days.includes(value))}))
  }
  function toggleSkill(skill) {
    setPreferences(previous => ({...previous,focusSkills:previous.focusSkills.includes(skill) ? previous.focusSkills.filter(value => value !== skill) : [...previous.focusSkills,skill]}))
  }
  async function save(event) {
    event.preventDefault(); setError(''); setMessage('')
    const [hour,minute] = preferences.startTime.split(':').map(Number)
    const maxSessions = Math.ceil(Math.ceil(preferences.weeklyHours * 60 / preferences.sessionMinutes) / preferences.days.length)
    if (hour*60+minute + maxSessions*preferences.sessionMinutes + Math.max(0,maxSessions-1)*15 >= 1440) { setError('These sessions would run past midnight. Start earlier, add study days, or reduce your weekly hours.'); return }
    setBusy(true)
    try {
      const updated = await api('/journey/schedule',{method:'PUT',body:JSON.stringify(preferences)})
      setActivePreferences({...preferences}); onSaved(updated); setEditing(false); setMessage('Timetable saved to your account.')
    } catch (err) { setError(err.message) } finally { setBusy(false) }
  }
  async function saveStudy(event,session) {
    event.preventDefault(); setBusy(true); setError(''); setMessage('')
    const minutes = Number(new FormData(event.currentTarget).get('minutes'))
    try {
      const entry = await api('/journey/study-log',{method:'PUT',body:JSON.stringify({sessionKey:session.sessionKey,date:session.date,skill:session.skill,plannedMinutes:session.duration,studiedMinutes:minutes,completed:minutes >= session.duration})})
      setLogs(previous => [...previous.filter(item => !(item.sessionKey === entry.sessionKey && item.date === entry.date)),entry]); setLogging(null); setMessage(`${minutes} minutes saved for ${session.skill}.`)
    } catch (err) { setError(err.message) } finally { setBusy(false) }
  }
  async function enableReminders() {
    setMessage(''); setError('')
    if (typeof Notification === 'undefined') { setError('This browser does not support study reminders. Your in-app timetable alerts will still work.'); return }
    const permission = await Notification.requestPermission()
    if (permission === 'granted') { localStorage.setItem(userKey('study-reminders'),'on'); setNotifications(true); setMessage('Browser reminders are enabled on this device while CareerForge is open.') }
    else setError('Browser reminders were not enabled. You can change notification permission in your browser settings.')
  }
  function downloadCalendar() {
    const content = calendarFile(sessions)
    const url = URL.createObjectURL(new Blob([content],{type:'text/calendar;charset=utf-8'}))
    const link = document.createElement('a'); link.href = url; link.download = `careerforge-${journey.role.toLowerCase().replaceAll(/[^a-z0-9]+/g,'-')}-timetable.ics`; link.click(); setTimeout(() => URL.revokeObjectURL(url),1000)
  }

  return <section className="study-planner panel"><div className="planner-heading"><div><span className="eyebrow">MAKE ROOM TO LEARN</span><h2>Your study week</h2><p>{activePreferences.weeklyHours} hours across {grouped.length} {grouped.length === 1 ? 'day' : 'days'} · {sessions.length} focused sessions · 15-minute breaks between consecutive sessions</p></div><div className="planner-actions"><button className="secondary" onClick={() => { setPreferences({...activePreferences}); setEditing(!editing) }}>{editing ? 'Cancel changes' : 'Adjust schedule'}</button><button className="secondary" disabled={!sessions.length} onClick={() => window.print()}>Save as PDF</button><button className="secondary" disabled={!sessions.length} onClick={() => setCalendarOpen(!calendarOpen)}>Add to calendar</button>{!notifications && <button className="secondary" onClick={enableReminders}>Enable browser reminders</button>}{notifications && <button className="secondary" onClick={() => { setNotifications(false); localStorage.removeItem(userKey('study-reminders')) }}>Turn browser reminders off</button>}</div></div>
    {calendarOpen && <section className="calendar-options"><h3>Choose your calendar</h3><p>Open a session in Google or Outlook, check its reminder setting, then press Save. Calendar reminders work without CareerForge open, subject to your device notification settings.</p><div className="calendar-session-list">{sessions.map((session,index) => <div key={index}><strong>{LABELS[session.day]} · {session.skill} · {readableTime(session.time)}–{readableTime(addMinutes(session.time,session.duration))}</strong><a href={calendarLink(session,'google')} target="_blank" rel="noopener noreferrer">Google Calendar ↗</a><a href={calendarLink(session,'outlook')} target="_blank" rel="noopener noreferrer">Outlook ↗</a></div>)}</div><button type="button" className="secondary" onClick={downloadCalendar}>Download week (.ics) for Apple / other calendars</button><small>This is a calendar file, not a PDF. Import it using your calendar’s Import function; Google file import requires a computer. It contains the next occurrence of each session and a 10-minute reminder. Import once to avoid duplicates.</small></section>}
    <ErrorNotice message={error}/>{message && <div className="notice success" role="status">{message}</div>}
    <div className="study-status"><div className="study-progress"><span><strong>{Math.round(studiedMinutes / 60 * 10) / 10}h</strong> studied this week</span><progress value={studiedMinutes} max={activePreferences.weeklyHours * 60} aria-label={`${studiedMinutes} of ${activePreferences.weeklyHours * 60} planned minutes studied`}/><small>{studiedMinutes} of {activePreferences.weeklyHours * 60} planned minutes logged</small></div><div className={`study-alert ${dayStatus.kind}`}><span aria-hidden="true">{dayStatus.kind === 'done' ? '✓' : dayStatus.kind === 'due' ? '!' : '→'}</span><div><strong>{dayStatus.title}</strong><p>{dayStatus.detail}</p></div></div></div>
    {editing && <form className="planner-settings" onSubmit={save}><div className="planner-fields"><label>Hours each week<input type="number" min="1" max="40" value={preferences.weeklyHours} onChange={event => setPreferences({...preferences,weeklyHours:Number(event.target.value)})}/></label><label>Start time<input type="time" required value={preferences.startTime} onChange={event => setPreferences({...preferences,startTime:event.target.value})}/></label><label>First session ends<input type="time" value={addMinutes(preferences.startTime,preferences.sessionMinutes)} readOnly/><small>Adjust start time or session length to change this.</small></label><label>Session length<select value={preferences.sessionMinutes} onChange={event => setPreferences({...preferences,sessionMinutes:Number(event.target.value)})}>{[30,45,60,90,120].map(value => <option value={value} key={value}>{value} minutes</option>)}</select></label></div>
      <fieldset><legend>Study days</legend><div className="choice-row">{DAYS.map(day => <label className={preferences.days.includes(day) ? 'selected' : ''} key={day}><input type="checkbox" checked={preferences.days.includes(day)} onChange={() => toggleDay(day)}/>{LABELS[day]}</label>)}</div><small>Choose at least one day. Extra sessions are distributed evenly across your selected days.</small></fieldset>
      <fieldset><legend>What do you want to study?</legend><div className="focus-choices">{available.map(step => <label className={preferences.focusSkills.includes(step.skill) ? 'selected' : ''} key={step.skill}><input type="checkbox" checked={preferences.focusSkills.includes(step.skill)} onChange={() => toggleSkill(step.skill)}/>{step.skill}</label>)}</div><small>{preferences.focusSkills.length ? 'The timetable will concentrate on your selected skills.' : 'No specific skills selected: CareerForge will take the first unfinished milestone through learning, practice, and recall before spreading your time further.'}</small></fieldset>
      <button className="primary" disabled={busy || !preferences.days.length}>{busy ? 'Saving…' : 'Save and rebuild timetable'}<Icon name="arrow" size={16}/></button></form>}
    <div className="week-strip" aria-label="Choose a study day">{DAYS.map(day => {
      const items = datedSessions.filter(session => session.day === day)
      const done = items.filter(session => logs.some(log => log.sessionKey === session.sessionKey && log.date === session.date && log.completed)).length
      return <button key={day} type="button" aria-pressed={selectedDay === day} onClick={() => { setSelectedDay(day); setLogging(null) }}><span>{LABELS[day]}</span><strong>{new Date(currentWeekDate(day)+'T12:00:00').getDate()}</strong><small>{items.length ? done === items.length && !logsLoading ? 'Complete' : items.reduce((sum,item) => sum+item.duration,0)+' min' : 'Rest day'}</small></button>
    })}</div>
    <div className="agenda-heading"><div><h3>{new Date(currentWeekDate(selectedDay)+'T12:00:00').toLocaleDateString(undefined,{weekday:'long',month:'short',day:'numeric'})}</h3><p>{journey.role}</p></div><span>Log actual time, even if you studied less.</span></div>
    {!datedSessions.some(item => item.day === selectedDay) && <div className="agenda-rest"><h3>A day with room to breathe.</h3><p>No sessions scheduled. Rest, catch up, or adjust your study days.</p></div>}
    {grouped.length ? <div className="schedule-board">{grouped.map(group => <article className={'schedule-day '+(group.day === selectedDay ? 'selected-day' : '')} key={group.day}><header><strong>{LABELS[group.day]}</strong><span>{group.sessions.reduce((sum,item) => sum + item.duration,0) / 60}h</span></header><div>{group.sessions.map(session => { const entry = logs.find(item => item.sessionKey === session.sessionKey && item.date === session.date); const canLog = session.date <= today; return <div className={`schedule-session ${entry?.completed ? 'is-logged' : ''}`} key={session.sessionKey}><time>{readableTime(session.time)}<br/>– {readableTime(addMinutes(session.time,session.duration))}</time><span className="session-rule"/><div><span>{session.phase}</span><h3>{session.skill}</h3><details className="session-instructions"><summary>Session guide</summary><p>{session.activity}</p>{journey.plan.find(step => step.skill === session.skill)?.resource?.url && <a href={journey.plan.find(step => step.skill === session.skill).resource.url} target="_blank" rel="noopener noreferrer">Open learning resource ↗</a>}</details><small>{session.duration} minutes · {new Date(`${session.date}T12:00:00`).toLocaleDateString(undefined,{day:'numeric',month:'short'})}</small><div className="session-log">{entry ? <><strong>{entry.completed ? '✓ Complete' : 'In progress'} · {entry.studiedMinutes}/{session.duration} min</strong><button className="text-button" onClick={() => setLogging(session.sessionKey)}>Edit</button></> : canLog ? <button className="text-button" onClick={() => setLogging(session.sessionKey)}>Log study time</button> : <span>Planned</span>}</div>{logging === session.sessionKey && <form className="session-log-form" onSubmit={event => saveStudy(event,session)}><label><span className="sr-only">Minutes studied</span><input name="minutes" type="number" min="0" max="720" required defaultValue={entry?.studiedMinutes || session.duration}/></label><button className="primary" disabled={busy}>Save minutes</button><button type="button" className="text-button" onClick={() => setLogging(null)}>Cancel</button></form>}</div></div>})}</div></article>)}</div> : <div className="planner-empty"><h3>Your timetable needs an active learning path.</h3><p>Generate your roadmap above, then choose the days and topics that fit your week.</p></div>}
    {logsLoading && <p className="planner-note">Loading your study record…</p>}
    <p className="planner-note">Your schedule repeats weekly. Minutes are self-reported; completing a session does not automatically prove proficiency.</p>
  </section>
}
