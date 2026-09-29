# API guide

Swagger UI is available at `/docs`; the OpenAPI document is `/v3/api-docs`.

All state-changing browser requests require `X-XSRF-TOKEN`, obtained from `GET /api/auth/csrf`. Authentication uses the session cookie. IDs identifying the acting user are never accepted.

## Authentication and profile

- `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`
- `POST /api/auth/refresh`, `GET /api/auth/me`
- `GET|PUT /api/profile`, `POST /api/profile/password`

## Instructor workflow

- `POST /api/instructor-applications`, `GET /api/instructor-applications/mine`
- `GET /api/instructor-applications` — admin
- `PATCH /api/instructor-applications/{id}/decision` — admin

## Courses and curriculum

- `GET|POST /api/courses`, `GET|PUT|DELETE /api/courses/{id}`
- `GET /api/courses/managed`
- `PATCH /api/courses/{id}/status`
- `PUT /api/courses/{id}/instructor` — admin
- `POST /api/courses/{id}/sections`
- `PUT|DELETE /api/sections/{id}`
- `PUT /api/courses/{id}/sections/order`
- `POST /api/sections/{id}/lessons`
- `PUT|DELETE /api/lessons/{id}`
- `PUT /api/sections/{id}/lessons/order`

Course writes support prerequisites, learning objectives, tags, and language. Lessons support a
description, video/resource URLs, preview access, duration, and independent published visibility.

## Enrollment, learning, and reviews

- `POST /api/courses/{id}/enroll`, `DELETE /api/courses/{id}/enrollment`, `GET /api/enrollments`
- `GET /api/courses/{id}/students` — course owner or admin
- `GET /api/learning/courses/{id}`
- `PUT /api/learning/lessons/{id}/completion`
- `POST /api/reviews`, `GET /api/courses/{id}/reviews`, `GET /api/courses/{id}/reviews/summary`
- `GET /api/reviews/mine`
- `PUT|DELETE /api/reviews/{id}`
- `GET /api/reviews/moderation`, `POST /api/reviews/{id}/moderate`

## Administration and dashboards

- `GET /api/dashboard/student|instructor|admin`
- `GET /api/admin/users`, `PATCH /api/admin/users/{id}/status|roles`
- `POST|PUT|DELETE /api/categories...` — admin; category list is public
- `GET /api/admin/audit-logs`

Collection endpoints cap page size server-side. Validation and authorization failures return a stable error object with `status`, `code`, `message`, and optional `fields`.
