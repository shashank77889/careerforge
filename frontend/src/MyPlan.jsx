import { useSearchParams, Link } from 'react-router-dom'
import Goals from './Goals'
import StudyPlanner from './StudyPlanner'
import { useResource } from './useResource'
import { PageHeading, Loading, ErrorNotice } from './ui'

export default function MyPlan() {
  const journey = useResource('/journey')
  const [params, setParams] = useSearchParams()
  const timetable = params.get('view') === 'timetable'
  return <>
    <PageHeading eyebrow="YOUR WEEK, YOUR GOALS" title="My Plan" description="Set your goals and make time to work toward them."/>
    <nav className="workspace-tabs" aria-label="Planning views"><button aria-pressed={!timetable} onClick={() => setParams({})}>Career goals</button><button aria-pressed={timetable} onClick={() => setParams({view:'timetable'})}>Study timetable</button></nav>
    {timetable ? journey.loading ? <Loading/> : journey.error ? <ErrorNotice message={journey.error} retry={journey.reload}/> : <>{journey.data.total ? <StudyPlanner key={journey.data.role} journey={journey.data} onSaved={journey.setData}/> : <section className="panel"><h2>Choose what you’ll study first</h2><p>Generate a learning path from your skill gaps, then come back to schedule your sessions.</p><Link className="primary" to="/learning">Build my learning path</Link></section>}</> : <div className="plan-goals"><Goals/></div>}
  </>
}
