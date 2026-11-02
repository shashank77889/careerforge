# Production release and operations

## Implemented
- Health endpoint checks MySQL with SELECT 1; unavailable database returns 503.
- Existing JWT, BCrypt, account ownership and password recovery with hashed, expiring single-use tokens.
- Login and registration have per-account and server-observed address attempt limits.
- Limits are bounded, in-memory, per instance, reset on restart; for multiple backend replicas replace with shared Redis-backed limits.
- Never trust arbitrary X-Forwarded-For supplied by clients. Configure proxy trust only against the hosting provider's documented topology.
- Account export excludes passwords, recovery tokens and unrevealed interview answer keys.
- Account deletion requires the current password and DELETE confirmation, removes owned records transactionally, and rejects old JWTs.
- Server SQL output is disabled to avoid recording resume/answer content in logs.
- User-facing render failures offer reload; existing server failures are logged in Railway.
- Account progress persists in MySQL. Job Match drafts are tab-scoped; interview drafts are device-scoped and saved answers sync through the API.

## Hosting checks (require operator account access)
Use the prod profile, provide JWT_SECRET, DATABASE_URL/USERNAME/PASSWORD and FRONTEND_ORIGIN.
Set PASSWORD_RESET_ENABLED=true, MAIL_FROM to a verified sender, PASSWORD_RESET_FRONTEND_URL to the exact public frontend origin, and BREVO_API_KEY.
No keys belong in frontend variables or Git. Brevo provider acceptance is not proof of inbox delivery.
Request reset for an existing test account; confirm Delivered in Brevo, open the email, reset password, sign in with the new password, reject old password/token, and verify old progress.
The operator must enter new passwords themselves during browser verification.

## Backups
Enable automated MySQL volume/database backups in the hosting account using an available retention plan.
Record retention and ownership. Database and volume backup capabilities/pricing depend on the hosting plan.
Before calling this production-ready, restore a backup into a separate temporary database and verify account, goal, skill, plan and study-log counts.
Never restore over the live database for a test. Restrict backup access and avoid unencrypted copies on shared storage.
Account deletion affects the active database; existing backups follow configured retention.
This repository does not activate or claim an externally configured backup schedule.

## Monitoring
Use Railway deployment logs for safe exception categories and uptime checks against /api/health.
Configure external uptime/error alert recipients in the hosting/monitoring account; no external alert subscription has been activated by these changes.
Check frontend error logs, deployment failures, database connection failures and reset-delivery failures.
Do not log request bodies, JWTs, passwords, reset links, or resume text.

## Release gate
Run backend mvn test and frontend lint/tests/build.
Verify separate-account isolation, upload extraction, plan completion, study logs, calendars and interviews.
Deploy backend and frontend; verify public API health and public browser flow.
Verify email delivery and a backup restore separately. Until these are proven, use a beta release label rather than claiming full production readiness.
