# Password recovery setup

The login page links to /forgot-password. Reset emails link to /reset-password#token=...
Tokens expire after 30 minutes, are stored only as SHA-256 hashes, and can be redeemed once.
Resetting preserves the user's ID and career data and invalidates existing JWT sessions.
Requests return the same response for known and unknown email addresses.

## Enable email on the backend

Configure these in Railway's backend service Variables (never in Vercel frontend variables or Git):
- PASSWORD_RESET_ENABLED=true
- PASSWORD_RESET_FRONTEND_URL: the exact public HTTPS frontend origin shared with users
- MAIL_HOST and MAIL_PORT: your email provider's SMTP server and STARTTLS port (typically 587)
- MAIL_USERNAME and MAIL_PASSWORD: your provider's SMTP credentials
- MAIL_FROM: a sender address verified with that provider
- MAIL_SMTP_AUTH=true
- MAIL_STARTTLS=true

Until enabled, requests return an honest 503 recovery-unavailable message.
Use the provider's SMTP credentials, not your normal mailbox password.
For local development use a local mail capture server and localhost frontend URL; never return reset tokens through a public API.
Use HTTP only for localhost; public reset links must use HTTPS.

The backend automatically applies db/2026-09-password-recovery.sql before production schema validation.
If you manage migrations externally, run the script first. It is safe to run again.
Do not delete password_recovery rows: their session_version revokes old access tokens even after reset tokens are cleared.

## Verify delivery after configuration

Using an account you control, request recovery, check inbox/spam, open the email link, and choose a new password.
Verify the old password and old sessions stop working; sign in with the new password and check saved progress.
An expired, malformed, or already-used link should offer a new reset request.
The server logs a generic delivery-failure event if SMTP fails; it never logs reset links or credentials.
The asynchronous email queue is bounded and in memory: requests interrupted by a backend restart need to be retried.
A persisted two-minute per-account cooldown protects mailboxes across instances.
The IP limit is per instance (20 attempts/minute); configure trusted proxy client-IP handling and edge rate limits before scaling.

Security design reference: [OWASP Forgot Password Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html).

## Railway: recommended HTTPS email delivery with Brevo

Railway Free, Trial and Hobby block outbound SMTP. Use the Brevo HTTPS API.
Create a Brevo account, verify the sending email/domain as required by Brevo, and create an API key (not an SMTP key).
Set these backend Railway Variables:

- BREVO_API_KEY: the private Brevo API key
- MAIL_FROM: your verified sender email address
- PASSWORD_RESET_FRONTEND_URL: the public frontend HTTPS origin
- PASSWORD_RESET_ENABLED: true

No SMTP credentials or OpenAI key are needed for this method. Brevo takes precedence when BREVO_API_KEY is present; failures do not silently switch providers.
Redeploy and run the delivery verification above. Live delivery requires an activated transactional email account and remaining provider quota.
Never commit the key or paste it into chat. Configuration alone is not proof of delivery.
