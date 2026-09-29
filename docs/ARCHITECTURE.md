# Architecture

## Shape

The repository is a deployable monorepo:

- `backend`: Java 21 / Spring Boot REST service
- `frontend`: React SPA served by unprivileged Nginx
- `compose.yml`: PostgreSQL, API, and web application on a private network

Backend packages are feature-oriented (`auth`, `user`, `course`, `enrollment`, `review`, `dashboard`, `category`) with shared `security`, `audit`, `exception`, `config`, and API primitives. Controllers perform transport concerns only. Services own transactions and authorization. Repositories own persistence. Mappers ensure entities never cross the API boundary.

## Data and transaction boundaries

Flyway is the sole schema authority; Hibernate runs in `validate` mode. Course/review changes and their audit record commit atomically. PostgreSQL enforces unique email, slug, enrollment, section position, lesson position, and `(student_id, course_id)` review constraints. Check constraints protect rating, progress, duration, status, and level domains. Optimistic versions on mutable aggregate roots detect lost updates.

Review rating aggregates are recalculated in the same transaction after create, update, delete, or moderation. At larger scale this can move to an outbox-backed projection without changing the REST contract.

## Authorization matrix

| Capability | Student | Instructor | Admin |
|---|---:|---:|---:|
| Browse published courses | Yes | Yes | Yes |
| Enroll / review | Enrolled only | No | No |
| Edit review | Own only | No | No |
| Manage course | No | Own only | All |
| Moderate review | No | Own course only | All |
| Admin metrics | No | No | Yes |

UI guards improve experience; method security and resource-level checks are authoritative.

