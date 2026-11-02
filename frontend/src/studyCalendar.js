const escapeText = value => String(value).replaceAll('\\','\\\\').replaceAll('\r','').replaceAll('\n','\\n').replaceAll(',','\\,').replaceAll(';','\\;')
const stamp = date => date.toISOString().replaceAll(/[-:]/g,'').replace(/\.\d{3}Z$/, 'Z')
export function nextOccurrence(day, time, now = new Date()) {
  const days = {SUNDAY:0,MONDAY:1,TUESDAY:2,WEDNESDAY:3,THURSDAY:4,FRIDAY:5,SATURDAY:6}
  const result = new Date(now); const [h,m] = time.split(':').map(Number)
  result.setDate(result.getDate() + (days[day] - result.getDay() + 7) % 7)
  result.setHours(h,m,0,0)
  if (result <= now) result.setDate(result.getDate()+7)
  return result
}
export function calendarLink(session, provider, now = new Date()) {
  const start=nextOccurrence(session.day,session.time,now), end=new Date(start.getTime()+session.duration*60000)
  const title=session.skill+' · '+session.phase
  if (provider === 'google') return 'https://calendar.google.com/calendar/render?'+new URLSearchParams({action:'TEMPLATE',text:title,dates:stamp(start)+'/'+stamp(end),details:session.activity})
  return 'https://outlook.live.com/calendar/0/deeplink/compose?'+new URLSearchParams({path:'/calendar/action/compose',rru:'addevent',subject:title,startdt:start.toISOString(),enddt:end.toISOString(),body:session.activity})
}
export function calendarFile(sessions, now = new Date()) {
  const lines=['BEGIN:VCALENDAR','VERSION:2.0','PRODID:-//CareerForge//Study Plan//EN','CALSCALE:GREGORIAN','METHOD:PUBLISH']
  sessions.forEach((session,index) => {
    const start=nextOccurrence(session.day,session.time,now), end=new Date(start.getTime()+session.duration*60000)
    lines.push('BEGIN:VEVENT','UID:careerforge-'+index+'-'+start.getTime()+'@careerforge','DTSTAMP:'+stamp(now),'DTSTART:'+stamp(start),'DTEND:'+stamp(end),'SUMMARY:'+escapeText(session.skill+' · '+session.phase),'DESCRIPTION:'+escapeText(session.activity),'BEGIN:VALARM','TRIGGER:-PT10M','ACTION:DISPLAY','DESCRIPTION:Study session starts in 10 minutes','END:VALARM','END:VEVENT')
  })
  lines.push('END:VCALENDAR')
  // RFC 5545 folds at 75 octets, including multibyte characters.
  return lines.map(line => { let output='', current='', size=0; for (const char of line) { const bytes=new TextEncoder().encode(char).length; if(size+bytes>75){output+=current+'\r\n';current=' ';size=1} current+=char;size+=bytes } return output+current }).join('\r\n')+'\r\n'
}
