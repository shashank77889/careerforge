# Update the existing CareerForge deployment

## Repositories

- Frontend: https://github.com/alexpetercarter-tech/careerforge-frontend
- Backend: https://github.com/alexpetercarter-tech/careerforge-backend

Both repositories use the main branch. Keep using the existing Vercel project and
backend service; creating a new project is not needed to publish updates.

## Frontend on Vercel

Run npm run lint and npm run build, then push the reviewed source to main.
Check the linked Vercel deployment for that exact commit and wait for Ready.
If automatic deployments are disabled, deploy the latest main commit from the
existing project's deployment controls.

The existing VITE_API_URL must point to the public backend with the /api suffix.
Vercel builds dist using the checked-in vercel.json, including SPA route fallback.
Source Sans 3 fonts are bundled with the build; no external font service is needed.
Keep node_modules, dist, logs, .vercel, and local environment files out of Git.

## Backend and database

The Spring Boot backend is deployed separately from Vercel. Preserve the existing
backend host, database, environment variables, and JWT_SECRET.

This release runs the additive SQL in
careerforge-backend/src/main/resources/db/2026-09-job-match.sql before schema validation.
It adds job_match_analyses without removing existing tables or account data and is
safe to rerun. For an account without CREATE permission, have the database administrator
apply this script first, then set SQL_INIT_MODE=never on the backend.
Earlier installations also need the existing journey and interview migrations.
Do not recreate the database or import local test users.

Push the backend source and deploy the matching main commit using its existing
hosting service. Confirm /api/health returns UP. If automatic deployment is
configured, pushing main triggers the existing release pipeline.

The startup ordering follows Spring Boot's SQL initialization mechanism:
https://docs.spring.io/spring-boot/4.1/how-to/data-initialization.html

## Verify the release

- Confirm the Vercel deployment uses the intended frontend commit and API URL.
- Sign in and check saved goals and learning progress.
- Open Job Match, analyze synthetic resume text, and reopen the saved analysis.
- Start a new 5-, 10-, or 20-question interview; existing sessions stay unchanged.
- Confirm the new typography appears and deep links such as /learning work.

Passwords, database credentials, JWT secrets, and AI keys belong only in backend
environment variables, never in VITE_ variables or Git.
