import {useState} from 'react'
import {Link} from 'react-router-dom'
import {useResource} from './useResource'
import {weakTopics} from './interviewFeedback.js'
export default function InterviewFeedback({session}) {
 const journey=useResource('/journey')
 const [selected,setSelected]=useState([])
 const mcq=session.questions.some(question=>question.options?.length)
 const areas=mcq ? weakTopics(session).map(item=>item.topic) : selected
 const topics=[...new Set(session.questions.map(question=>question.topic))]
 return <section className="interview-feedback"><h3>{mcq ? 'What to practise next' : 'Choose up to two areas to practise'}</h3><p>{mcq ? 'Based on incorrect answers in this session only.' : 'Written answers are not automatically graded. Compare your answers with the review points below, then choose your focus.'}</p>
 {!mcq && <div className="feedback-topics">{topics.map(topic=><button className="secondary" aria-pressed={selected.includes(topic)} disabled={!selected.includes(topic) && selected.length===2} key={topic} onClick={()=>setSelected(previous=>previous.includes(topic)?previous.filter(value=>value!==topic):[...previous,topic])}>{topic}</button>)}</div>}
 {mcq && !areas.length && <p>No incorrect answers in this session. Try a different session to broaden your practice.</p>}
 {areas.map(topic=>{
 const normalized=topic.replace(/^(Resume|Practice gap) · /,'').toLowerCase()
 const step=journey.data.plan?.find(item=>normalized===item.skill.toLowerCase() || normalized.startsWith(item.skill.toLowerCase()+' '))
 const question=session.questions.find(item=>item.topic===topic)
 return <article key={topic}><h4>{topic}</h4><p>{step ? step.resource.practice : question?.explanation || question?.reviewPoints?.[0] || 'Review the explanation below, then solve a similar problem without notes.'}</p>{step ? <Link to={'/learning?skill='+encodeURIComponent(step.skill)}>Practise {step.skill} in Learning →</Link> : <Link to="/interviews">Try another practice session →</Link>}</article>
 })}
 {journey.error && <p>Learning links are temporarily unavailable. You can still review the answers below.</p>}
 </section>
}
