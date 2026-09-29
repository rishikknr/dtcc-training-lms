# Security operations

## Production checklist

- Generate unique `DB_PASSWORD` and `SEED_PASSWORD` values. Never commit `.env`.
- Set `SEED_ENABLED=false` outside local development. Create the first admin through a controlled bootstrap process.
- Terminate TLS at the ingress and set `COOKIE_SECURE=true`.
- Set `CORS_ORIGINS` to exact HTTPS origins. Never use `*` with credentialed requests.
- Use a least-privilege PostgreSQL runtime role and, where policy requires, a separate Flyway migration role.
- Restrict actuator, Swagger, database, and container-management endpoints at the network layer.
- Export application and audit events to restricted, append-only storage or a SIEM.
- Back up PostgreSQL, test restores, and define retention/deletion rules for profiles, reviews, and audit evidence.
- Run dependency, image, SAST, secret, and migration checks in CI. Use Dependabot or Renovate for updates.
- Apply distributed request and connection limits at the ingress. The application login throttle is per process and is defense in depth.

## Identity and sessions

Passwords are BCrypt-hashed at cost 12. Authentication uses an HTTP-only, SameSite session cookie with session fixation protection and a three-session concurrency cap. Mutations require a CSRF token. Login responses are generic, and failed attempts are throttled and persisted into temporary account lockout state.

Authenticated requests resynchronize account enabled-state and authorities from PostgreSQL. Admin disabling therefore revokes an existing session, and approved instructor authority does not depend on browser-supplied state.

## Authorization and data handling

- Registration always assigns `STUDENT` and ignores extra role/identity fields.
- Instructor authority comes only from an admin-reviewed server workflow.
- Services validate ownership, enrollment, and moderation scope after loading the resource.
- Request DTOs expose only writable fields; owner IDs, roles, aggregate ratings, status metadata, and moderation actors are not mass assignable.
- Response DTOs prevent password hashes, lock state, and internal entity graphs from leaking.
- Protected lesson content is returned only from an enrollment-authorized learning endpoint.
- JPA parameters, bounded pagination, request-size limits, React text rendering, CSP, anti-framing, referrer policy, and restrictive CORS reduce injection, XSS, disclosure, and abuse risk.

Never render lesson or review content through `dangerouslySetInnerHTML` without a maintained sanitizer.

## Operational limitations

For higher-assurance deployments, add MFA or enterprise SSO, a distributed rate limiter, centralized session registry/revocation, malware scanning for future file uploads, and append-only off-database audit export.

## Vulnerability reporting

Report issues privately to the platform security owner with the endpoint/version, reproduction, impact, and sanitized proof of concept. Never include live credentials or personal data.
