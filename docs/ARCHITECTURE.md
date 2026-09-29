# Architecture

## Repository shape

Academy is a deployable monorepo:

- `backend`: Java 21 and Spring Boot REST API
- `frontend`: React, TypeScript, Vite, and Tailwind SPA
- `compose.yml`: PostgreSQL, API, and Nginx-hosted frontend
- `docs`: architecture, API, security operations, and STRIDE model

## Backend package contract

The backend is feature-first. A feature owns its persistence, transport, mapping, validation, business rules, and resource authorization instead of placing all controllers or entities in global layers.

```text
com.academy.lms
├── auth
│   ├── controller
│   ├── dto/request
│   ├── dto/response
│   ├── security/authorization
│   └── service
├── user
│   ├── controller
│   ├── dto/request
│   ├── dto/response
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
├── course
│   ├── controller
│   ├── dto/request
│   ├── dto/response
│   ├── entity
│   ├── mapper
│   ├── repository
│   ├── security/authorization
│   ├── service
│   └── validation
├── curriculum
│   ├── controller
│   ├── dto/request
│   ├── dto/response
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
├── enrollment, learning, review, instructor, category, dashboard
│   └── equivalent feature-owned layers where applicable
└── common
    ├── api
    ├── audit
    ├── config
    ├── domain
    ├── exception
    ├── security
    └── validation
```

Empty ceremonial layers are avoided. For example, `dashboard` has no entity because it is a read model; `auth` reuses the user repository and mapper rather than duplicating them.

## Responsibilities

- Controllers handle HTTP mapping, validation activation, authenticated-principal extraction, and status codes only.
- Services own transactions, workflow rules, and orchestration.
- Authorization components enforce ownership or moderation scope on loaded resources.
- Validators enforce cross-field and lifecycle rules such as publish readiness.
- Repositories own persistence queries and pagination.
- Mappers are the only route from entities to API response DTOs.
- Entities protect state transitions through domain methods. JPA entities never leave the API boundary.

## Principal workflows

### Instructor onboarding

1. Every public registration creates a `STUDENT`; role fields in JSON have no effect.
2. A student submits one instructor application.
3. An admin approves or rejects it. Rejections require a reason and can be resubmitted.
4. Approval grants `INSTRUCTOR` inside the server transaction.
5. Account state and authorities are synchronized from the database on every authenticated request, so role grants and account disabling affect existing sessions.

### Course and curriculum lifecycle

1. An instructor creates a draft owned by their authenticated account.
2. Only that instructor or an admin may change the course or its curriculum.
3. Sections and lessons support create, edit, delete, atomic full-list reordering, resources,
   previews, and independent lesson publication.
4. Publishing requires a category, cover image, section, and at least one published lesson.
5. Published courses with learners cannot return to draft; they can be archived.
6. Only empty drafts can be deleted, preventing accidental destruction of learner records.

### Learning and progress

Only actively enrolled students receive protected, published lesson content from `/api/learning`.
Students can cancel and later reactivate an enrollment without losing prior progress. Completion is
stored per enrollment and lesson. The enrollment aggregate is recalculated transactionally when
completion or published curriculum changes, and reaches 100% only when every current published
lesson is complete.

### Reviews

Enrollment, ownership, course-management authority, and admin status are resolved on the server. PostgreSQL additionally enforces one review per student/course. Published-rating aggregates are recalculated in the same transaction after create, edit, delete, hide, or restore.

## Data and transactions

Flyway is the sole schema authority and Hibernate runs with `ddl-auto=validate`. PostgreSQL enforces unique email, slug, enrollment, curriculum position, lesson completion, and review constraints; foreign keys and checks protect relationship and enum integrity. Mutable aggregate roots use optimistic versions. Important mutations and their audit rows share the business transaction.

## Authorization matrix

| Capability | Student | Instructor | Admin |
|---|---:|---:|---:|
| Browse published courses | Yes | Yes | Yes |
| Apply to teach | Yes | N/A | N/A |
| Approve instructor access | No | No | Yes |
| Enroll, leave, re-enroll, and learn | Yes | Student role required | Student role required |
| Review a course | Enrolled only | Student role required | Student role required |
| Edit review | Own only | Own only if also student | Admin delete only |
| Create course | No | Yes | Yes |
| Manage course/curriculum | No | Own only | All |
| Moderate reviews | No | Own courses only | All |
| Manage users/categories | No | No | Yes |
| View course student roster | No | Own only | All |
| Read audit history | No | No | Yes |

Frontend guards improve usability; method security and resource-level checks remain authoritative.
