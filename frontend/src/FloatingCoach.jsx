import { useRef, useState } from 'react'
import { CareerCoach } from './JourneyWidgets'
import { Icon } from './ui'
import { useResource } from './useResource'

export default function FloatingCoach() {
  const availability = useResource('/journey/coach')
  const dialog = useRef(null)
  const trigger = useRef(null)
  const [opened, setOpened] = useState(false)
  const [started, setStarted] = useState(false)
  function close() { dialog.current.close(); setOpened(false); trigger.current?.focus() }
  if (!availability.data?.available || availability.error) return null
  return <>
    <button ref={trigger} className="coach-launcher" aria-label="Open AI coach" title="Need help? Open your coach" aria-haspopup="dialog" aria-expanded={opened} aria-controls="floating-coach" onClick={() => { setStarted(true); setOpened(true); dialog.current.showModal() }}><Icon name="chat" size={24}/><span>Ask coach</span></button>
    <dialog ref={dialog} id="floating-coach" className="floating-coach" aria-labelledby="floating-coach-title" onCancel={() => setOpened(false)} onClose={() => setOpened(false)} onClick={event => { if (event.target === dialog.current) { const box = dialog.current.getBoundingClientRect(); if (event.clientX < box.left || event.clientX > box.right || event.clientY < box.top || event.clientY > box.bottom) close() } }}>
      <header><div><h2 id="floating-coach-title">Your career coach</h2><small>Help when you need it</small></div><button className="icon-button" aria-label="Close coach" onClick={close}>×</button></header>
      <div className="floating-coach-content" onClick={event => { if (event.target.closest('a[href^="/"]')) close() }}>{started && <CareerCoach/>}</div>
    </dialog>
  </>
}
