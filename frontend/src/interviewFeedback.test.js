import test from 'node:test'
import assert from 'node:assert/strict'
import {weakTopics} from './interviewFeedback.js'
test('feedback ranks observed mistakes and limits focus to two topics',()=>{
 const q=(topic,correctIndex=0)=>({topic,correctIndex,options:['a','b','c','d']})
 const result=weakTopics({questions:[q('SQL'),q('Java'),q('Java'),q('Git')],answers:['1','0','1','0']})
 assert.deepEqual(result.map(item=>item.topic),['SQL','Java'])
})
test('correct and ungraded answers do not become invented weak areas',()=>{
 assert.deepEqual(weakTopics({questions:[{topic:'Written',options:[]},{topic:'Java',options:['a','b'],correctIndex:0}],answers:['short','0']}),[])
})
