# STRIDE threat model

## Assets and boundaries

Primary assets are credentials, sessions, roles, private lesson content, profile data, course ownership, enrollment/progress state, reviews, moderation decisions, and audit evidence. Trust boundaries exist between browser/API, API/database, admin/user, instructor/student, and runtime/operator configuration.

| Threat | Abuse case | Controls | Residual / operational action |
|---|---|---|---|
| Spoofing | Stolen password/session; identity field added to JSON | BCrypt 12, generic errors, throttling, lockout, fixation protection, HTTP-only/SameSite cookies, CSRF, TLS-ready secure cookies, server principal extraction | Add MFA/SSO and centralized revocation for higher assurance |
| Tampering | Browser changes owner, role, position, rating count, or progress | Narrow request DTOs, Bean Validation, domain methods, ownership checks, full-list reorder validation, JPA parameters, FKs/checks/uniques, optimistic locking | Alert on repeated authorization and stale-update failures |
| Repudiation | User denies publishing, role approval, moderation, or completion | Actor/resource/IP audit records in the business transaction; immutable timestamps | Export to append-only restricted storage for regulated use |
| Information disclosure | Draft course or paid/private lesson content leaks | Status-aware course lookup, dedicated enrollment-gated learning API, DTO allowlists, generic errors, exact CORS origins, CSP and security headers | Periodic access tests and log-redaction review |
| Denial of service | Brute force, huge pages/bodies, expensive searches/reorders | Login throttling, account lockout, page caps, reorder caps, 1 MB request limits, indexed search/catalog fields, session cap | Add distributed edge limits, timeouts, and database resource controls |
| Elevation of privilege | Student selects ADMIN/INSTRUCTOR; instructor manages another course | Registration always STUDENT, admin-reviewed instructor workflow, DB-synchronized authorities, method RBAC, resource authorization, no generic role mutation endpoint | Protect admin accounts with MFA and step-up authorization |

## Security invariants covered by tests

- Unauthenticated and CSRF-free mutations fail.
- Students cannot access admin/course-authoring APIs.
- Registration payloads cannot grant admin or instructor roles.
- Instructor access remains unavailable until admin approval.
- Unenrolled users cannot spoof another identity to review or access learning content.
- Duplicate reviews conflict at both service and database layers.
- Students cannot edit another student's review.
- Instructors cannot edit or moderate another instructor's resources.
- SQL-injection-shaped catalog input is handled as data.
- Flyway migrations and Hibernate mappings validate against real PostgreSQL in Testcontainers.
