# CareerForge matching research prototype

## Methods
- Existing structured baseline: catalog skills and aliases, important phrases, and requirement token coverage.
- Concept-v1: deterministic, inspectable dictionary covering twelve concepts with conservative negation/aspiration filtering. Evidence excerpts and dictionary limitations are shown.
- Semantic review: Xenova/all-MiniLM-L6-v2, q8, normalized mean-pooled embeddings, cosine similarity. Transformers.js 4.3.0 runs in a browser worker using WASM. No paid inference service or AI key. Network access is needed for the initial model/runtime download.
- Each of up to 12 extracted requirements is compared with up to 80 saved resume passages (500 characters per passage). The highest-similarity passage is displayed even below threshold. These limits are disclosed.
- Semantic scores do not change the structured score, saved skills, or proficiency. No claims are added to resumes automatically.

## Reproduce the pilot
Open a new Job Match analysis, expand Research lab, and run the benchmark.
The bundled 16-pair synthetic development set includes eight positive pairs, four unrelated negatives, and four hard negatives.
Labels represent relevant evidence, not verified skill or actual candidate suitability.
The token-coverage baseline uses 0.50; the initial semantic threshold is an exploratory 0.55.
Precision, recall, F1, confusion counts and all examples are shown. Change the semantic threshold to inspect trade-offs and export JSON.
The lexical pilot is a simple token-coverage baseline, not the application's composite score.
A zero denominator produces zero precision/recall/F1.
Model weights are downloaded from the named upstream model's default revision; freeze a model commit before any formal experiment.

## Limitations and research next steps
This is an experimental implementation and development pilot, not a validated MTech research result.
Do not interpret similarity as entailment: negation, future intent, different qualifications, experience duration and seniority can produce false matches.
The concept dictionary is intentionally limited, not general semantic NLP.
Semantic results currently recalculate on demand; the saved source passages and structured/concept analysis persist.
Older saved analyses remain readable but need re-analysis to add source passages.
Collect consented, de-identified resume/job pairs, define an annotation rubric, use independent raters and report agreement.
Separate train/development/test sets by job family or source to avoid leakage. Freeze model, dataset and threshold before held-out evaluation.
Report confidence intervals, per-role errors, latency, memory and fairness limitations; compare exact-keyword, alias, concept and embedding ablations.
Do not use these results for automated hiring decisions.

## Privacy and runtime
Source passages are stored with the user's analysis and protected by the existing ownership checks. Deleting the analysis removes them.
The UI discloses this before submission. Uploaded binaries are not saved by Job Match.
Inference text is not sent to Hugging Face; only model/runtime assets are requested.
Cancel terminates the worker. Navigating away terminates it as well. Model-load failure leaves the keyword report usable.
Downloaded model caching is browser-dependent and low-memory devices may be unable to run it.

## Verification
Backend: mvn test (including old-result compatibility and ownership checks).
Frontend: npm run lint; node --test src/semanticMath.test.js src/studyCalendar.test.js; npm run build.
Browser smoke test: sign in with a test account, submit a paraphrased resume, run semantic comparison, run the pilot, reload and reopen saved analysis.
Password-recovery delivery is a separate deployment configuration issue; these tests do not prove inbox delivery.
