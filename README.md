# CareerForge

A full-stack career preparation app with resume skill extraction, role-based learning plans, saved mock interviews, job-description comparison, and optional AI career guidance.

## Technology
React 19, Vite, Java 21, Spring Boot, Spring Security, JWT authentication, and MySQL. An H2 test profile supports temporary local previews.

## Repository layout
- `frontend/`: React application, frontend setup and feature documentation.
- `backend/`: Spring Boot API, database migrations, tests, and deployment configuration.

## Run locally
1. Install Java 21, Node.js compatible with the frontend dependencies, and MySQL.
2. Create a MySQL database named `careerforge`.
3. Set backend environment variables using `backend/.env.example` as a reference. These examples are documentation; the app does not automatically load that file. Supply your database credentials through the process environment.
4. In `backend`, run `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`). The API uses port 8080.
5. In `frontend`, run `npm ci`, then `npm run dev`. Open the address printed by Vite.

See [frontend documentation](frontend/README.md) for the temporary H2 preview and detailed feature behavior. See [frontend deployment](frontend/DEPLOYMENT.md), [backend hosting notes](backend/FREE_HOSTING.md), and [deployment readiness](backend/DEPLOYMENT_READINESS.md) before deploying.

## Checks
- Frontend: `npm run lint` and `npm run build` from `frontend`.
- Backend: `./mvnw test` from `backend`.

## Status and limitations
This is a portfolio project in development. Role readiness and job matches are learning guidance and keyword comparisons, not validated hiring or ATS scores. AI advice is optional and requires server-side provider credentials. Live hosting availability has not been verified for this publication package. Five-student usability testing remains pending in `frontend/STUDENT_TEST.md`.

## Privacy
Local credentials, uploaded resumes, runtime data, logs, and generated build/dependency folders are excluded. Use your own environment variables; never commit real secrets or user data.
