import { useEffect, useRef, useState } from 'react'
import { fixtures } from './semanticFixtures'
import { metrics, MODEL, THRESHOLD } from './semanticMath'

export default function SemanticReview({ analysis, research = false }) {
  const worker = useRef(null)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')
  const [rows, setRows] = useState(null)
  const [benchmark, setBenchmark] = useState(null)
  const [threshold, setThreshold] = useState(THRESHOLD)
  useEffect(() => () => worker.current?.terminate(), [])
  const requirements = [...new Set((analysis?.result.requirements || []).map(item => item.text))].slice(0, 12)
  const passages = analysis?.result.resumePassages || []
  function run(kind) {
    if (busy) return
    setBusy(true); setMessage('Starting…')
    worker.current?.terminate()
    const next = new Worker(new URL('./semantic.worker.js', import.meta.url), { type: 'module' })
    worker.current = next
    const finish = () => { setBusy(false); next.terminate(); worker.current = null }
    next.onerror = () => { setMessage('Semantic comparison could not start in this browser. Your saved analysis is still available.'); finish() }
    next.onmessage = ({ data }) => {
      if (data.type === 'progress') setMessage(data.message)
      else if (data.type === 'complete') {
        if (kind === 'benchmark') setBenchmark(data.rows)
        else setRows(data.rows)
        setMessage('Comparison complete. Review the evidence before changing your resume.'); finish()
      } else { setMessage(data.message); finish() }
    }
    next.postMessage(kind === 'benchmark' ? { kind, fixtures } : { kind, requirements, passages })
  }
  function cancel() {
    worker.current?.terminate(); worker.current = null; setBusy(false); setMessage('Comparison cancelled. Your saved analysis is unchanged.')
  }
  function download() {
    const blob = new Blob([JSON.stringify({ model: MODEL, dataset: 'careerforge-synthetic-pilot-v1', threshold, lexicalThreshold: 0.5, createdAt: new Date().toISOString(), semantic: metrics(benchmark, 'semantic', threshold), lexical: metrics(benchmark, 'lexical', 0.5), rows: benchmark }, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob), link = document.createElement('a')
    link.href = url; link.download = 'careerforge-pilot-evaluation.json'; link.click(); setTimeout(() => URL.revokeObjectURL(url), 1000)
  }
  return <section className="panel semantic-review">
    {!research && <>    <span className="eyebrow">LOOK BEYOND EXACT WORDS</span><h2>Semantic evidence review</h2>
    <p>Compare the meaning of requirements and resume passages using MiniLM. The model downloads from Hugging Face on first use; inference runs on your device without sending your text to an AI provider. No paid API key is needed.</p>
    <p className="match-privacy">Experimental: a high similarity can still describe a negation, a different qualification, or a skill you only intend to learn. Similarity is not proof and does not change your job-match score. Reviews are calculated on demand and are not saved. Up to 12 detected requirements and 80 resume passages are compared.</p>
    <button className="secondary" disabled={busy || !requirements.length || !passages.length} onClick={() => run('match')}>Compare meaning</button>
    {(!requirements.length || !passages.length) && <p>Run a new analysis with complete responsibilities and resume text to enable this comparison.</p>}
    {rows && <div className="requirement-list">{rows.map((row, i) => <article key={i}><strong>{row.requirement}</strong><p>{row.evidence}</p><small>Cosine similarity: {row.similarity.toFixed(3)} · {row.similarity >= THRESHOLD ? 'Related wording — verify the claim' : 'Weak similarity — review manually'}</small></article>)}</div>}
    </>}
    {busy && <button className="text-button" onClick={cancel}>Cancel</button>}
    <p role="status">{message}</p>
    {research && <div className="research-content"><h2>Compare matching methods</h2>
      <p>Run a reproducible pilot on 16 hand-labelled synthetic pairs, including negation and aspiration traps. This small development set is not independent validation, a hiring benchmark, or evidence of real-world accuracy. All failures remain visible.</p>
      <button className="secondary" disabled={busy} onClick={() => run('benchmark')}>Run pilot benchmark</button>
      {benchmark && <><label>Semantic review threshold: {threshold.toFixed(2)}<input type="range" min="0" max="1" step="0.05" value={threshold} onChange={e => setThreshold(Number(e.target.value))}/></label>
        <p>Changing this threshold explores trade-offs on this development set; it does not validate a production threshold. Lexical baseline uses requirement-token coverage ≥ 0.50.</p>
        <div className="semantic-table"><table><caption>Pilot classification metrics</caption><thead><tr><th>Method</th><th>Precision</th><th>Recall</th><th>F1</th><th>TP / FP / TN / FN</th></tr></thead><tbody>{[['Token coverage', 'lexical', 0.5], ['MiniLM', 'semantic', threshold]].map(([name, key, cutoff]) => { const m = metrics(benchmark, key, cutoff); return <tr key={key}><th>{name}</th><td>{m.precision.toFixed(2)}</td><td>{m.recall.toFixed(2)}</td><td>{m.f1.toFixed(2)}</td><td>{m.tp} / {m.fp} / {m.tn} / {m.fn}</td></tr> })}</tbody></table></div>
        <button className="text-button" onClick={download}>Download results (JSON)</button>
        <div className="requirement-list">{benchmark.map(row => <article key={row.id}><strong>{row.id} · Expected: {row.expected ? 'relevant evidence' : 'unsupported'}</strong><p>{row.requirement}</p><p>{row.evidence}</p><small>MiniLM {row.semantic.toFixed(3)} · token coverage {row.lexical.toFixed(3)} · {((row.semantic >= threshold) === row.expected) ? 'Agrees with label' : 'Disagrees with label'}</small></article>)}</div>
      </>}
    </div>}
  </section>
}
