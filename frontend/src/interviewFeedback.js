export function weakTopics(session) {
  const groups = new Map()
  session.questions.forEach((question,index) => {
    if (!question.options?.length || !Number.isInteger(question.correctIndex)) return
    const group = groups.get(question.topic) || {topic:question.topic,wrong:0,total:0}
    group.total++
    if (session.answers[index] !== String(question.correctIndex)) group.wrong++
    groups.set(question.topic,group)
  })
  return [...groups.values()].filter(group=>group.wrong>0).sort((a,b)=>b.wrong/b.total-a.wrong/a.total || b.wrong-a.wrong || a.topic.localeCompare(b.topic)).slice(0,2)
}
