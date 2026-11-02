const courses = [
  {skills:['Python'],title:'CS50 Python — full university course',provider:'Harvard CS50 / freeCodeCamp',url:'https://www.youtube.com/watch?v=nLRL_NcnK-4',source:'https://cs50.harvard.edu/python/',kind:'YouTube · Free'},
  {skills:['SQL'],title:'CS50 SQL — databases course',provider:'Harvard CS50 / freeCodeCamp',url:'https://www.youtube.com/watch?v=WXk7yDqsKxs',source:'https://cs50.harvard.edu/sql/',kind:'YouTube · Free'},
  {skills:['Java'],title:'Java Programming Masterclass',provider:'Tim Buchalka and instructors',url:'https://www.udemy.com/course/java-the-complete-java-developer-course/',source:'https://www.udemy.com/course/java-the-complete-java-developer-course/',kind:'Udemy · Paid, price varies'},
  {skills:['HTML','CSS','JavaScript'],title:'The Complete Full-Stack Web Development Bootcamp',provider:'Dr. Angela Yu',url:'https://www.udemy.com/course/the-complete-web-development-bootcamp/',source:'https://www.udemy.com/course/the-complete-web-development-bootcamp/',kind:'Udemy · Paid, price varies'},
]
export default function VideoResources({skill}) {
  const matched=courses.filter(course=>course.skills.includes(skill))
  if(!matched.length) return null
  return <details className="video-resources"><summary>Prefer a video course?</summary>{matched.map(course=><article key={course.url}><small>{course.kind}</small><h4>{course.title}</h4><p>{course.provider}</p><a href={course.url} target="_blank" rel="noopener noreferrer">Open course ↗</a> · <a href={course.source} target="_blank" rel="noopener noreferrer">Course details</a></article>)}<small>Optional alternatives, checked September 2026. Use the relevant chapters and complete the practice task. Buying a course is not required; free resources remain available.</small></details>
}
