# Free hosting migration plan — not yet provisioned
Prepared 2026-10-03. Keep Vercel frontend, move backend to Render Free and MySQL-compatible
data to TiDB Cloud Starter with spending limit 0. Do not enable paid tiers.

Render: https://render.com/docs/free
750 free instance hours/workspace/month; idle services sleep after 15 minutes.
First request can be slow. Memory and outbound usage limits apply.
TiDB: https://docs.pingcap.com/tidbcloud/select-cluster-tier/
Starter free quotas: 5 GiB row data and 50 million request units/month per eligible instance.
Keep spending limit 0; reaching limits may suspend operations rather than permit overage.
Backups: https://docs.pingcap.com/tidbcloud/backup-and-restore-serverless/
Free Starter has daily backups retained for one day, not long-term retention.

## Migration gate
1. Owner creates/signs into Render and TiDB, accepts their terms, selects free options only.
2. Create empty Starter database. Use its TLS-verified JDBC connection information in Render
   environment variables; never commit credentials. Validate MySQL compatibility with the
   actual schema, foreign keys, pessimistic locking and transactions before any cutover.
3. Export Railway database into protected storage and import into the empty destination.
   Keep the original intact. Do not put resume/account data in Git or GitHub artifacts.
4. Deploy render.yaml; enter secrets in provider UI. Verify health, login, saved progress,
   upload, interview answer/finish and learning review. Test memory with PDF/DOCX uploads.
5. Restore a daily backup into a separate free test destination when quota permits and
   compare counts and synthetic account records. Record result; no restore is yet verified.
6. Freeze writes briefly for a final data copy before changing Vercel's API rewrite to the
   verified Render domain. Verify public flows, then resume writes.
7. Keep original until migration is verified. Disabling/removing Railway requires review.

No accounts, databases or hosting services were created by this preparation.
No payment was authorized. Uptime workflow must use the final public API URL.
