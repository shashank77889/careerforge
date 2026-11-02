import test from 'node:test'
import assert from 'node:assert/strict'
import { cosine, metrics, lexical } from './semanticMath.js'
test('cosine handles orthogonal, identical, opposite and zero vectors', () => {
 assert.equal(cosine([1,0], [0,1]), 0)
 assert.equal(cosine([2,0], [3,0]), 1)
 assert.equal(cosine([1,0], [-1,0]), -1)
 assert.equal(cosine([0,0], [0,0]), 0)
})
test('metrics expose false positives and false negatives', () => {
 const result = metrics([{s:.8,expected:true},{s:.8,expected:false},{s:.2,expected:true},{s:.2,expected:false}], 's', .5)
 assert.deepEqual(result, {tp:1,fp:1,tn:1,fn:1,precision:.5,recall:.5,f1:.5,accuracy:.5})
 assert.equal(metrics([], 's', .5).f1, 0)
})
test('token coverage is case insensitive and not substring matching', () => {
 assert.equal(lexical('Java Java', 'JAVA'), 1)
 assert.equal(lexical('Java', 'JavaScript'), 0)
})
