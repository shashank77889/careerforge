import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, userPath } from './api'
import { ErrorNotice, Icon } from './ui'
export default function Onboarding({ journey, onSaved }) {
 const [step,setStep]=useState(0),[busy,setBusy]=useState(false),[error,setError]=useState(''),[uploaded,setUploaded]=useState(false)
 const navigate=useNavigate()
 async function upload(event) {
  event.preventDefault();const file=new FormData(event.currentTarget).get('file')
  if(!file?.size || file.size>10*1024*1024 || !/\.(pdf|docx)$/i.test(file.name)){setError('Choose a PDF or DOCX resume under 10 MB.');return}
  setBusy(true);setError('')
  try{const body=new FormData();body.append('file',file);await api(userPath('resumes'),{method:'POST',body});setUploaded(true);setStep(1)}catch(err){setError(err.message)}finally{setBusy(false)}
 }
 async function direction(event) {
  event.preventDefault();setBusy(true);setError('')
  const values=Object.fromEntries(new FormData(event.currentTarget))
  try{await api('/journey',{method:'PUT',body:JSON.stringify({role:values.role,weeklyHours:Number(values.weeklyHours)})});setStep(2)}catch(err){setError(err.message)}finally{setBusy(false)}
 }
 async function finish() {
  setBusy(true);setError('')
  try{onSaved(await api('/journey/plan',{method:'POST'}));navigate('/learning?view=skills')}catch(err){setError(err.message)}finally{setBusy(false)}
 }
 return <section className="panel onboarding"><span className="eyebrow">GET STARTED · STEP {step+1} OF 3</span><ol className="setup-steps">{['Your skills','Your direction','Your first task'].map((label,i)=><li key={label} aria-current={step===i?'step':undefined} className={step>=i?'active':''}>{i+1}. {label}</li>)}</ol>
 {step===0?<><h2>Start with what you know</h2><p>Upload a resume to find your skills, or add them manually afterward.</p><form onSubmit={upload}><label>Resume (PDF or DOCX, up to 10 MB)<input name="file" type="file" accept=".pdf,.docx" required/></label><button className="primary" disabled={busy}>{busy?'Reading your resume…':'Upload & continue'}<Icon name="arrow"/></button></form><button className="text-button" disabled={busy} onClick={()=>{setError('');setStep(1)}}>I’ll add my skills manually</button></>:step===1?<><h2>Choose a direction and pace</h2><form onSubmit={direction}><label>Target role<select name="role" defaultValue={journey.role}>{journey.roles.map(role=><option key={role}>{role}</option>)}</select></label><label>Study hours per week<input name="weeklyHours" type="number" min="1" max="40" required defaultValue={journey.weeklyHours}/></label><button className="primary" disabled={busy}>{busy?'Saving…':'Save & continue'}</button></form></>:<><h2>Your workspace is ready</h2><p>{uploaded?'Your resume skills are saved. Review their accuracy and rate your proficiency before following the plan.':'Add your current skills and proficiency to make the initial plan more relevant.'}</p><button className="primary" disabled={busy} onClick={finish}>{busy?'Creating your plan…':'Create plan & review skills'}<Icon name="arrow"/></button></>}
 <ErrorNotice message={error}/></section>
}
