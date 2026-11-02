import {test} from 'node:test'
import assert from 'node:assert/strict'
import {calendarFile,calendarLink,nextOccurrence} from './studyCalendar.js'
test('calendar exports use UTC, valid folded lines and reminder alarms',()=>{
 const now=new Date('2026-09-11T10:00:00Z'),session={day:'MONDAY',time:'19:00',duration:60,skill:'SQL',phase:'Learn',activity:'Study commas, semicolons; and Unicode α'.repeat(8)}
 const file=calendarFile([session],now)
 assert.match(file,/BEGIN:VALARM\r\nTRIGGER:-PT10M/)
 assert.match(file,/DTSTART:\d{8}T\d{6}Z/)
 assert.match(file,/DTEND:\d{8}T\d{6}Z/)
 for(const line of file.split('\r\n')) assert.ok(new TextEncoder().encode(line).length<=75)
 assert.ok(file.endsWith('END:VCALENDAR\r\n'))
 assert.match(file,/commas\\,/)
 assert.ok(nextOccurrence('MONDAY','19:00',now)>now)
 const google=new URL(calendarLink(session,'google',now))
 assert.equal(google.hostname,'calendar.google.com')
 assert.equal(google.searchParams.get('text'),'SQL · Learn')
 const dates=google.searchParams.get('dates').split('/')
 assert.notEqual(dates[0],dates[1])
 const outlook=new URL(calendarLink(session,'outlook',now))
 assert.equal(new Date(outlook.searchParams.get('enddt'))-new Date(outlook.searchParams.get('startdt')),3600000)
})
