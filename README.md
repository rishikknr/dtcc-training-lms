# Academy LMS

A production-oriented learning management system built with Java 21, Spring Boot 3, PostgreSQL, React, TypeScript, Vite, and Tailwind CSS.

## Run the complete stack

Prerequisites: Docker 24+ and Docker Compose.

```bash
cp .env.example .env
# Replace DB_PASSWORD before exposing the stack beyond localhost.
docker compose up --build
```

Open [http://localhost:5173](http://localhost:5173). Swagger UI is proxied by the backend at [http://localhost:5173/docs](http://localhost:5173/docs) when accessed directly through a backend port, or at `http://localhost:8080/docs` when running the backend locally.

Development seed accounts all use the password you set in `SEED_PASSWORD`:

- `student@academy.local`
- `instructor@academy.local`
- `admin@academy.local`

Disable seed data with `SEED_ENABLED=false` in non-development environments.

## Local development

Start PostgreSQL, then:

```bash
cd backend
DB_URL=jdbc:postgresql://localhost:5432/lms DB_USER=lms DB_PASSWORD=... mvn spring-boot:run

cd ../frontend
npm ci
npm run dev
```

Java 21 is required. The frontend dev server proxies `/api` to port 8080.

## Verification

```bash
make test
make build
```

Backend tests include Mockito authorization tests and Testcontainers integration tests against real PostgreSQL. Docker must be running for integration tests.

## API and security model

Authentication uses an HTTP-only, SameSite session cookie. Mutations require the double-submit CSRF token returned by `GET /api/auth/csrf`. The SPA sends `X-XSRF-TOKEN`; clients never supply an effective user ID or role. Course ownership, enrollment, review ownership, and moderation scope are resolved from the authenticated server-side principal.

See [Architecture](docs/ARCHITECTURE.md), [STRIDE threat model](docs/THREAT_MODEL.md), and [Security operations](docs/SECURITY.md).
