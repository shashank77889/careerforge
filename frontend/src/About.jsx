import { PageHeading } from './ui'

const features = [
  ['Resume & skills', 'Upload a PDF or DOCX resume to extract skills, add skills manually and review your proficiency.'],
  ['Career readiness', 'Choose a target role, compare your skills with its requirements and identify areas to improve. Readiness reflects your saved skill ratings, not a hiring prediction.'],
  ['Learning & study plans', 'Follow resources for missing skills, explore selected video courses, track milestones and study time, and download your timetable or add sessions to a calendar. Browser reminders require the planner to remain open.'],
  ['Job match', 'Compare a job description with your resume. Review matching skills, missing keywords and truthful improvement suggestions. Current matching uses keywords and aliases.'],
  ['Interview practice', 'Practice role-based written questions or 30 technical and aptitude MCQs. Review your score, answers and explanations after completing an MCQ session. Questions are not yet personalized to your resume.'],
  ['Goals & applications', 'Save career goals, track job applications and return to your saved progress.'],
  ['Optional AI & recovery', 'AI coaching and AI-generated MCQs require a configured AI service. Password recovery requires email delivery to be configured. These services may be unavailable.'],
]

export default function About() {
  return <>
    <PageHeading title="About CareerForge" description="Your career preparation workspace."/>
    <section className="panel settings-section about-section" aria-labelledby="about-title">
      <span className="eyebrow">ABOUT</span><h2 id="about-title">CareerForge</h2>
      <p>A career preparation workspace for students, freshers and job seekers: understand your skills, build a study plan and prepare for your next opportunity.</p>
      <dl className="about-features">{features.map(([title, description]) => <div key={title}><dt>{title}</dt><dd>{description}</dd></div>)}</dl>
      <footer className="developer-credit"><span>Designed & developed by</span><strong>Shashank</strong></footer>
    </section>
  </>
}
