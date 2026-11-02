import { useState } from 'react'
import { api, clearSession, userKey } from './api'
import { ErrorNotice } from './ui'
export default function AccountControls({ onDeleted }) {
 const [busy,setBusy]=useState(false), [error,setError]=useState(''), [confirm,setConfirm]=useState(false)
 async function exportData() {
  setBusy(true);setError('')
  try {
   const data=await api('/account/export')
   const url=URL.createObjectURL(new Blob([JSON.stringify(data,null,2)],{type:'application/json'}))
   const link=document.createElement('a');link.href=url;link.download='careerforge-account.json';link.click();setTimeout(()=>URL.revokeObjectURL(url),1000)
  } catch(err) {setError(err.message)} finally {setBusy(false)}
 }
 async function remove(event) {
  event.preventDefault();setBusy(true);setError('')
  try {
   await api('/account',{method:'DELETE',body:JSON.stringify(Object.fromEntries(new FormData(event.currentTarget)))})
   const prefix=userKey('')
   for (const storage of [localStorage,sessionStorage]) for(const key of Object.keys(storage)) if(key.startsWith(prefix)) storage.removeItem(key)
   localStorage.removeItem('careerforge-theme:'+localStorage.getItem('userId'))
   clearSession();onDeleted()
  } catch(err) {setError(err.message)} finally {setBusy(false)}
 }
 return <section className="panel settings-section"><h2>Your data</h2><p>Download your saved progress, goals, resume text, and interview answers. Keep the exported file private.</p>
 <button className="secondary" disabled={busy} onClick={exportData}>{busy?'Please wait…':'Download my data'}</button>
 <details><summary>Delete my account</summary><p>This permanently removes your account and its saved data from the active database. Copies in existing backups expire under the hosting retention policy.</p>
 {!confirm?<button className="text-button danger" onClick={()=>setConfirm(true)}>Review account deletion</button>:<form onSubmit={remove}><p role="alert">This cannot be undone. Download your data first if you need it.</p><label>Current password<input name="password" type="password" autoComplete="current-password" required/></label><label>Type DELETE to confirm<input name="confirmation" pattern="DELETE" required autoComplete="off"/></label><div className="form-actions"><button className="secondary" type="button" disabled={busy} onClick={()=>setConfirm(false)}>Cancel</button><button className="primary" disabled={busy}>Permanently delete account</button></div></form>}</details><ErrorNotice message={error}/></section>
}
