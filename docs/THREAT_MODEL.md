# STRIDE threat model

## Assets and boundaries

Primary assets are account credentials, session identifiers, private lesson content, user profile data, course ownership, enrollment state, reviews, moderation decisions, and audit evidence. Trust boundaries exist at browser/API, API/database, and operator/runtime configuration interfaces.

| Threat | Example | Controls | Residual / operational action |
|---|---|---|---|
| Spoofing | Stolen password or session | BCrypt cost 12, generic login errors, IP+identity throttling, account lockout, session fixation protection, HTTP-only/SameSite cookies, TLS-ready secure-cookie setting | Add MFA/SSO and centralized session revocation for higher assurance deployments |
| Tampering | Client changes owner, role, rating state | Request-specific DTOs, Bean Validation, server-derived principal, service ownership checks, JPA parameters, FKs, checks, unique constraints, optimistic locking, CSRF | Alert on repeated authorization failures |
| Repudiation | User denies publishing/moderation | Timestamped audit rows for auth and important mutations with actor/resource/IP; logs commit atomically | Export append-only to a restricted SIEM for regulated use |
| Information disclosure | Draft lessons or account internals leak | DTO allowlists, hidden lesson content unless enrolled/owner/admin, generic errors, no password fields, CORS allowlist, CSP and security headers | Periodic access review and log-redaction checks |
| Denial of service | Login brute force, unbounded search | Login cache throttling, DB account lockout, page-size cap 50, request-size cap, indexed catalog, maximum sessions | Put global/distributed rate limiting and connection limits at the ingress |
| Elevation of privilege | Student claims ADMIN or instructor edits another course | Roles loaded from DB, registration always STUDENT, method RBAC, resource checks for every mutable aggregate, no role mutation endpoint | Separate admin provisioning and require step-up auth |

## Abuse cases covered by tests

- unauthenticated writes and CSRF-free writes fail;
- student access to admin/course-authoring APIs fails;
- a registration payload cannot assign ADMIN;
- an unenrolled identity cannot spoof another student to review;
- a student cannot edit another student's review;
- an instructor cannot edit or moderate another instructor's resources.

