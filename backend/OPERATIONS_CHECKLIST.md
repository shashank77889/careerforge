# Release evidence, not assumptions

## Email recovery
Sign in to Brevo transactional logs and Railway deployment logs.
Request a reset for an existing live test account. Search Railway for Password recovery.
- No matching account: the email is not registered in this database.
- Cooldown active: wait two minutes before retrying.
- HTTP 401/403: correct the backend API key/provider access.
- HTTP 400: inspect Brevo verified sender/account activation.
- Provider accepted: locate the message ID in Brevo; acceptance is not delivery.
Record Delivered or the exact bounce reason. Then reset, sign in, and check saved progress.
Do not paste keys, reset tokens, or private email contents into issues.

## Backup and restore gate
In Railway, enable a supported automated backup schedule for MySQL and record retention.
Create an isolated restore destination; never overwrite production for a test.
Restore a provider backup there. Compare record counts for users, skills, career_goals,
career_journeys, learning_steps, study_logs, interview_sessions and job_match_analyses
using the actual table names from the restored schema.
Run the backend against the restored database with a separate JWT secret and no email/AI credentials.
Check a synthetic account's goals, learning completion, schedule and interview answers.
Record backup timestamp, restore timestamp, operator, counts and pass/fail. Remove the
isolated instance only after review. This file does not prove a backup exists.

## Uptime alerts
The availability workflow checks public frontend and database-backed API twice an hour.
It activates only after push to the repository default branch with Actions/schedules enabled.
GitHub schedules can be delayed and are not a real-time SLA monitor.
Enable notifications for failed workflows in the operator's GitHub settings.
Run workflow_dispatch once, confirm a successful run, and confirm the intended recipient
receives failure notifications before claiming alerts are operational.

## Mobile beta checklist
Use a synthetic account on 390px and an actual Android/iPhone browser:
signup/login; upload resume or manual skills; choose role; generate path;
start milestone; attempt task; submit practical review; refresh; verify notes;
schedule/log minutes; refresh; resume interview; complete/review; job match;
sign out/in; password reset from inbox; verify all saved records remain.
Automated/browser checks supplement, rather than replace, feedback from real students.
