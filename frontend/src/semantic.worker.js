import { pipeline, env } from '@huggingface/transformers'
import { cosine, lexical, MODEL } from './semanticMath'
env.allowLocalModels = false
env.backends.onnx.wasm.numThreads = 1
let extractor
self.onmessage = async ({ data }) => {
  try {
    self.postMessage({ type: 'progress', message: 'Loading the language model. The first download may take a few minutes…' })
    extractor ??= await pipeline('feature-extraction', MODEL, { dtype: 'q8', device: 'wasm' })
    const texts = data.kind === 'benchmark'
      ? data.fixtures.flatMap(row => [row.requirement, row.evidence])
      : [...data.requirements, ...data.passages]
    const vectors = []
    for (let i = 0; i < texts.length; i += 8) {
      self.postMessage({ type: 'progress', message: 'Comparing text: ' + Math.min(i + 8, texts.length) + ' / ' + texts.length })
      const output = await extractor(texts.slice(i, i + 8), { pooling: 'mean', normalize: true })
      vectors.push(...output.tolist())
    }
    if (data.kind === 'benchmark') {
      const rows = data.fixtures.map((row, i) => ({ ...row, semantic: cosine(vectors[i * 2], vectors[i * 2 + 1]), lexical: lexical(row.requirement, row.evidence) }))
      self.postMessage({ type: 'complete', rows })
    } else {
      const rows = data.requirements.map((requirement, i) => {
        let best = -Infinity, index = 0
        data.passages.forEach((passage, j) => {
          const score = cosine(vectors[i], vectors[data.requirements.length + j])
          if (score > best) { best = score; index = j }
        })
        return { requirement, evidence: data.passages[index], similarity: best }
      })
      self.postMessage({ type: 'complete', rows })
    }
  } catch {
    self.postMessage({ type: 'error', message: 'The model could not load or run. Check your connection and available memory, then retry. Your saved keyword analysis is still available.' })
  }
}
