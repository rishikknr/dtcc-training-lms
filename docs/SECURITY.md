# Security operations

## Production checklist

- Set a unique, randomly generated `DB_PASSWORD`; do not use `.env.example` values.
- Terminate TLS at the ingress and set `COOKIE_SECURE=true`.
- Set `CORS_ORIGINS` to exact HTTPS origins; never use `*` with credentials.
- Set `SEED_ENABLED=false`; provision instructor/admin roles through a controlled operator workflow.
- Use a least-privilege PostgreSQL role scoped to this database. Use a separate migration identity if policy requires it.
- Keep `/actuator` exposure limited to health/info and restrict it at the network layer.
- Forward structured application and PostgreSQL logs to the SIEM; restrict audit table access.
- Back up PostgreSQL, test restore, and define retention/deletion policy for profile and audit data.
- Run dependency, container, SAST, secret, and migration scans in CI. Renovate/Dependabot should keep lockfiles current.
- Put distributed request limits at the ingress. The included login throttle is per-process and is defense in depth, not a cluster-wide control.

## Web defenses

The application uses parameterized JPA queries, output-as-text React rendering, CSP, anti-framing, no-sniff and referrer headers, explicit DTOs, request limits, CSRF tokens, and credentialed-origin CORS. Do not render lesson/review content with `dangerouslySetInnerHTML` without a vetted sanitizer.

## Vulnerability reporting

Report privately to the platform security owner. Include affected endpoint/version, reproduction steps, impact, and any proof of concept. Do not include real credentials or personal data.

