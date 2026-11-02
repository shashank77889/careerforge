import { useState } from 'react'
import { useResource } from './useResource'
import { Icon, ErrorNotice, Loading } from './ui'

export default function PortfolioProject({ role }) {
  const project = useResource(`/journey/portfolio-project?role=${encodeURIComponent(role)}`)
  const [copied, setCopied] = useState(false)
  if (project.loading) return <section className="portfolio-project"><Loading/></section>
  if (project.error) return <ErrorNotice message={project.error} retry={project.reload}/>
  const data = project.data
  async function copyBrief() {
    const brief = `${data.title}\n\nProblem: ${data.problem}\n\nSkills: ${data.skills.join(', ')}\n\nDeliverables:\n- ${data.deliverables.join('\n- ')}\n\nProof checklist:\n- ${data.proofChecklist.join('\n- ')}\n\nResume bullet template:\n${data.resumeBulletTemplate}`
    try { await navigator.clipboard.writeText(brief); setCopied(true); setTimeout(() => setCopied(false), 1800) }
    catch { setCopied(false) }
  }
  return <section className="portfolio-project"><div className="portfolio-copy"><span className="eyebrow">A PROJECT THAT PROVES THE SKILLS</span><div className="portfolio-title"><div className="project-glyph">✦</div><div><span>{data.role} portfolio brief</span><h2>{data.title}</h2></div></div><p>{data.problem}</p><div className="skill-chips">{data.skills.map(skill => <span key={skill}>{skill}</span>)}</div><button className="secondary" onClick={copyBrief}><Icon name="book" size={16}/>{copied ? 'Brief copied' : 'Copy complete brief'}</button></div><div className="portfolio-details"><div><span className="eyebrow">WHAT TO SHIP</span><ol>{data.deliverables.map(item => <li key={item}>{item}</li>)}</ol></div><details><summary>What makes this portfolio-ready?</summary><ul>{data.proofChecklist.map(item => <li key={item}>{item}</li>)}</ul><strong>Resume bullet template</strong><p className="resume-bullet">{data.resumeBulletTemplate}</p></details></div></section>
}
