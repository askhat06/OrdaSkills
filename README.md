# Orda Skills Backend

This repository is the Spring Boot backend for Orda Skills. It is the contract source of truth for auth, courses, lessons, enrollments, admin course CRUD, and admin lesson video workflows.

The canonical frontend is not stored in this repository. Per the current project architecture, the real frontend lives in the separate `oyn_front` / `jiyuu` React project and integrates with this backend over HTTP.

## What This Repo Contains

- Spring Boot API on port `7777`
- Flyway-managed database schema
- JWT auth and role-based access rules
- rate limiting for public auth and enrollment endpoints
- admin course CRUD endpoints
- admin lesson video upload endpoints
- backend tests and integration docs

## What This Repo Does Not Contain

- the canonical React frontend
- gallery/photoshoots/vacancy legacy UI code
- `json-server` data or frontend build tooling

If you are a frontend AI agent, use [docs/PROJECT_BLUEPRINT.md](C:/GPT/OrdaSkills/docs/PROJECT_BLUEPRINT.md) as the handoff guide before changing `oyn_front`.

## Repository Layout

- `src/main/java/...` Spring Boot application code
- `src/main/resources/...` YAML profiles and Flyway migrations
- `src/test/java/...` backend tests
- `docs/PROJECT_BLUEPRINT.md` cross-repo backend/frontend alignment guide
- `docs/DEMO_RUNBOOK.md` demo instructions for backend plus external frontend
- `docs/REQUIREMENTS_MATRIX.md` requirement tracking

## API Summary

### Public

- `GET /api/health`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/courses`
- `GET /api/courses/{slug}`
- `GET /api/courses/{courseSlug}/lessons/{lessonSlug}`
- `POST /api/enrollments`

### Authenticated

- `GET /api/auth/me`
- `GET /api/enrollments`

### Admin

- `GET /api/admin/courses`
- `GET /api/admin/courses/{courseId}`
- `POST /api/admin/courses`
- `PUT /api/admin/courses/{courseId}`
- `DELETE /api/admin/courses/{courseId}`
- `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload`
- `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete`
- `DELETE /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video`

## Frontend Integration Notes

These backend behaviors are important for the external frontend:

- default profile is `local`
- non-local runtime requires explicit env vars for PostgreSQL, JWT, and S3-compatible media
- `401` covers missing, expired, malformed, and deleted-user JWT cases
- `403` covers authenticated users without the required role
- `409` is used for duplicate enrollment and unsafe course deletion
- `429` is used for login, registration, and enrollment rate limiting
- `204` responses must not be parsed as JSON

For the full frontend-agent handoff, see:

- [docs/PROJECT_BLUEPRINT.md](C:/GPT/OrdaSkills/docs/PROJECT_BLUEPRINT.md)

## Run Locally

### Backend

Maven Wrapper scripts are included:

- Windows: `.\mvnw.cmd spring-boot:run`
- macOS/Linux: `./mvnw spring-boot:run`

Default local behavior:

- profile: `local`
- database: H2 in-memory
- H2 console: `http://localhost:7777/h2-console`
- media provider: in-memory
- port: `7777`

Local H2 values:

- JDBC URL: `jdbc:h2:mem:elearningdb`
- Username: `sa`
- Password: blank

### External frontend

Run the canonical frontend from its own repository and point it at this backend on `http://localhost:7777`.

The expected frontend env pattern is documented in the frontend project blueprint shared by the user, not in this repo.

## Postgres / Prod-Style Configuration

Use [.env.example](C:/GPT/OrdaSkills/.env.example) as the reference for required backend env vars when running `postgres` or `prod`.

Required areas:

- PostgreSQL connection
- JWT secret
- S3-compatible media storage settings

Example startup with explicit profile:

- Windows: `.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres`
- macOS/Linux: `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres`

## Testing

Backend tests live under `src/test/java`.

Coverage includes:

- auth flow
- deleted-user JWT fallback to `401`
- anonymous enrollment non-mutation rules
- rate limiting
- admin course CRUD
- admin lesson video upload behavior
- `prod` / `postgres` profile startup checks

Planned command once Java is installed:

- backend: `.\mvnw.cmd test`

## Demo Flow

The intended MVP demo still is:

1. open the frontend landing page from `oyn_front`
2. enroll a test student
3. log in
4. open the lesson viewer
5. inspect the enrollment in H2 or PostgreSQL

Use [docs/DEMO_RUNBOOK.md](C:/GPT/OrdaSkills/docs/DEMO_RUNBOOK.md) for the backend-side operator flow.

## Requirement Status

Requirement tracking lives in [docs/REQUIREMENTS_MATRIX.md](C:/GPT/OrdaSkills/docs/REQUIREMENTS_MATRIX.md).
