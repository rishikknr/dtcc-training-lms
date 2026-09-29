# Academy LMS

A production-oriented learning management system built with Java 21, Spring Boot 3, Spring Security, PostgreSQL/Flyway, React, TypeScript, Vite, and Tailwind CSS.

## Run the complete stack

Prerequisites: Docker 24+ and Docker Compose.

```bash
cp .env.example .env
# Set strong DB_PASSWORD and SEED_PASSWORD values.
docker compose up --build
```

Open the application at [http://localhost:5173](http://localhost:5173) and Swagger at [http://localhost:8080/docs](http://localhost:8080/docs).

Development seed accounts use the password configured in `SEED_PASSWORD`:

- `student@academy.local`
- `instructor@academy.local`
- `admin@academy.local`

Set `SEED_ENABLED=false` outside local development.

## Roles and instructor access

Public registration always creates a student. A user becomes an instructor through the application workflow:

1. Sign in as a student and open **Become an instructor**.
2. Submit expertise, teaching motivation, and an optional portfolio.
3. Sign in as an admin, open **Admin console**, and approve the request.
4. The applicant's existing session receives database-backed instructor authority on its next authenticated request.

Roles cannot be selected during registration or changed from the frontend.

## Product capabilities

- Secure registration, login/logout, profiles, and password changes
- Admin-reviewed instructor onboarding and immediate account-state enforcement
- Draft, publish, archive, guarded delete, search, filter, sort, and pagination
- Full section/lesson create, edit, delete, reorder, preview, visibility, and resource workflows
- Active-enrollment-gated learning content, leave/rejoin lifecycle, rosters, and per-lesson progress
- Student, instructor, and admin dashboards with real metrics
- One-review-per-enrollment, owner edit/delete, scoped moderation, rating distributions, and aggregation
- Admin user status, categories, courses, instructor applications, reviews, and audit history APIs
- DTO-only APIs, resource authorization, audit logs, request validation, and optimistic locking

## Local development

Start PostgreSQL, then:

```bash
cd backend
DB_URL=jdbc:postgresql://localhost:5432/lms \
DB_USER=lms DB_PASSWORD=... SEED_PASSWORD=... mvn spring-boot:run

cd ../frontend
npm ci
npm run dev
```

Java 21 is required. The Vite server proxies `/api` to port 8080.

## Verification

```bash
make test
make build
```

Backend tests include JUnit/Mockito authorization checks and Spring Boot/Testcontainers tests against PostgreSQL. Docker must be running for integration tests.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [API guide](docs/API.md)
- [STRIDE threat model](docs/THREAT_MODEL.md)
- [Security operations](docs/SECURITY.md)

Authentication uses an HTTP-only SameSite session cookie. Mutations require the CSRF token returned by `GET /api/auth/csrf`. The server derives identity, role, ownership, enrollment, and moderation authority from trusted state.
