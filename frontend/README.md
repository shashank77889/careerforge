# CareerForge

A React + Spring Boot career preparation workspace: resume evidence → skill review → target role → personalized roadmap → practice → reflection and reassessment.

## Run locally

Start the backend on port 8080, then run `npm install` and `npm run dev` in this directory. Open http://127.0.0.1:5173. Vite proxies `/api` to the backend. `npm run build` produces `dist`; `npm run lint` checks the frontend.

For a temporary preview without MySQL, run from the backend directory:

```powershell
mvn spring-boot:run '-Dspring-boot.run.useTestClasspath=true' '-Dspring-boot.run.profiles=test'
```

That profile uses an in-memory H2 database. Its data is temporary, and generative AI is disabled in tests. Normal backend startup uses the configured MySQL database. In local development, CareerForge keeps a generated signing key under the ignored backend `.local` directory, so valid sessions survive restarts. Production requires `JWT_SECRET`.

## Connected features

- Target career and weekly study hours saved to the user's account.
- Self-reported readiness: `100 × sum(min(current proficiency, role target)) / sum(role targets)`.
- Skill coverage shown separately from proficiency and learning completion.
- Roadmaps for missing **and developing** skills across all nine supported roles. Initial generation excludes skills that already meet their targets.
- Editorial resources, estimated practice hours, helpful prerequisites, practical tasks, progress status, and reflection notes for each milestone.
- Provider and source-type labels on every learning resource, with an explicit CareerForge editorial-review marker.
- A role-specific portfolio project brief built from the learner's current skill gaps, with deliverables, an evidence checklist, and a resume-bullet template that requires real measured results.
- A local-time dashboard greeting that welcomes the signed-in user by first name and updates throughout the day.
- Plan refresh adds newly relevant milestones without discarding previous progress. Existing milestones remain as practice history even when skill ratings later meet targets. Changing role preserves each role's plan.
- Database-backed completion and notes, role-specific readiness snapshots, and Markdown roadmap export.
- Account-backed last-page memory so returning users reopen where they left off. Goal-form and interview-answer drafts are also kept in the current browser until successfully saved.
- Saved mock interview sessions for all nine roles, with four role-specific questions, one behavioral question, progress history, and review prompts. Users can resume unfinished interviews or review completed answers.
- PDF/DOCX resume extraction with whole-word matching, DOCX table text, and source excerpts from the latest five resumes. Mentions start at 0% proficiency until the user reviews them.
- Local job-description comparison against the supported skill catalog. This is keyword coverage, not an ATS score.
- Guided career advice with an optional generative AI provider, described below.
- Existing goals, application tracking, registration, sign-in, and sign-out remain available.

Role targets are CareerForge learning benchmarks, not validated employer requirements. Course completion does not update proficiency. Readiness is not a verified assessment, placement prediction, or guarantee of employment. Resource practice-time estimates are ours, not provider course durations. Prerequisites are guidance, not enforced enrollment restrictions.

## Optional AI career coach

Set `OPENAI_API_KEY` and `OPENAI_MODEL` in the **backend process environment**. Choose a Responses-compatible model available to your account. Never put the key in Vite variables or commit it. Restart the normal backend (the `test` profile deliberately disables AI).

The UI labels generated replies as AI and asks the user to consent before sending their question, role, study hours, skill summary, and completed milestone count to OpenAI. It does not automatically send account identifiers, names, email addresses, resumes, reflection notes, or job descriptions. A user can still type personal data into their question; the UI asks them to avoid doing so.

Requests use `store: false`, bounded input/output, a timeout, and one request per ten seconds per user per backend instance. Provider failures return explicitly labeled guided advice. This is a single-turn coach; no tools, generated links, automatic profile updates, or hiring decisions are delegated to the model. Without credentials it provides guided, rule-based advice and never claims that advice is AI-generated.

Implementation reference: [OpenAI Responses API](https://developers.openai.com/api/reference/typescript/resources/beta/subresources/responses/methods/create). `store: false` is not a blanket zero-retention guarantee; review your provider account's data controls before launch.

## MySQL deployment

The backend includes `application-prod.properties` and additive migrations in `database/2026-09-career-journey.sql` and `database/2026-09-mock-interviews.sql`. Apply them to an existing CareerForge database on staging first, then validate against a backup-restored database. The production profile uses schema validation rather than automatic updates.

Configure `DATABASE_URL` (a JDBC MySQL URL), `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET` (at least 32 random bytes represented as a secret string), and `FRONTEND_ORIGIN` (the exact HTTPS origin). Start with the `prod` profile. Serve the frontend with SPA fallback to `index.html` and reverse-proxy `/api`; alternatively set `VITE_API_URL` including `/api` at frontend build time.

The backend now includes a production Dockerfile and public `/api/health` readiness endpoint. The frontend includes a Vercel SPA configuration and `.env.example`. Follow [DEPLOYMENT.md](DEPLOYMENT.md) for a concrete Render + MySQL + Vercel route.

Before a public launch, verify the MySQL migration and backup/restore workflow, configure HTTPS and gateway-level authentication/AI rate limits, set provider spending limits, and provide account recovery and data-deletion workflows. These infrastructure and operational tasks are not performed by this local implementation. Current JWTs are kept in browser local storage; a production authentication review should consider HTTP-only cookies, CSRF protections, and token rotation together. The additive migration has not been applied to a live MySQL database here.

## Verification

Run `mvn test` from the backend directory. Real HTTP integration tests exercise registration, goals, skills, DOCX resume extraction, applications, CORS, cross-user denial, all role catalogs, readiness calculations, plan generation and refresh, saved notes, completion independence from skill scores, role switching, snapshots, local job matching, the no-key coach path, page restoration, and interview creation, saving, completion, conflicts, and account isolation.

Live AI responses require provider credentials and are not covered by the offline suite. Tests run on H2 and are not a substitute for MySQL staging tests. Learning progress previously stored only in browser local storage is not silently imported; the new server-backed roadmap begins separately.
