# E-learning Platform Backend

Spring Boot backend for a Kazakhstan-focused e-learning platform. The app supports the learner MVP flow plus an admin-only lesson video upload workflow.

## What this backend does

- serves a seeded course catalog
- returns course landing pages and lesson viewer payloads
- supports anonymous enrollment with DB persistence
- supports JWT-based register, login, and current-user endpoints
- lets authenticated users view enrollments with role-based access rules
- lets admins upload, finalize, and delete lesson videos through storage-backed APIs

## Tech stack

- Java 17
- Spring Boot 3.5.11
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Security
- JWT via JJWT
- Flyway
- H2 support
- PostgreSQL profile support
- AWS S3 SDK for S3-compatible media storage

## Main data model

- `Course`
- `Lesson`
- `PlatformUser`
- `Enrollment`
- `LessonVideoUpload`

## API routes

### Public routes

- `GET /api/health`
- `GET /api/courses`
- `GET /api/courses/{slug}`
- `GET /api/courses/{courseSlug}/lessons/{lessonSlug}`
- `POST /api/enrollments`
- `POST /api/auth/register`
- `POST /api/auth/login`

### Authenticated routes

- `GET /api/auth/me`
- `GET /api/enrollments`

Notes:

- students can view only their own enrollments
- admins can view all enrollments and optionally filter by `courseSlug`, `email`, or both

### Admin-only media routes

- `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload`
- `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete`
- `DELETE /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video`

The media flow is two-step:

1. initialize an upload to get an object key, upload URL, required headers, and playback URL
2. upload the object to storage, then call the `complete` endpoint so the lesson starts serving the new `videoUrl`

## Example requests

### Register

```bash
curl -X POST http://localhost:7777/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test Student",
    "email": "test.student@example.com",
    "password": "Password123!",
    "locale": "en-KZ"
  }'
```

### Course landing page

```bash
curl http://localhost:7777/api/courses/digital-skills-kz
```

### Lesson viewer

```bash
curl http://localhost:7777/api/courses/digital-skills-kz/lessons/intro-to-digital-skills
```

### Enroll

```bash
curl -X POST http://localhost:7777/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{
    "courseSlug": "digital-skills-kz",
    "fullName": "Test Student",
    "email": "test.student@example.com",
    "locale": "en-KZ"
  }'
```

### List enrollments

```bash
curl http://localhost:7777/api/enrollments \
  -H "Authorization: Bearer <token>"
```

### Initiate admin video upload

```bash
curl -X POST http://localhost:7777/api/admin/courses/digital-skills-kz/lessons/intro-to-digital-skills/video-upload \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "lesson.mp4",
    "contentType": "video/mp4",
    "sizeBytes": 1048576
  }'
```

## Demo flow

1. Open the course landing page.
2. Submit an enrollment for a test learner.
3. Register or log in to obtain a JWT.
4. Open the lesson viewer.
5. Use authenticated `GET /api/enrollments` to confirm saved enrollment data.
6. If running with the `local` profile, optionally inspect H2 at `http://localhost:7777/h2-console`.
7. If using an admin account, initialize and complete a lesson video upload to replace the lesson's `videoUrl`.

## Running the app

There is no Maven wrapper in this repo, so use a globally installed Maven or run the app from your IDE.

### Default runtime behavior

The code currently sets the default Spring profile to `postgres`.

That means:

- launching the app without an explicit profile will activate PostgreSQL-oriented config
- `prod` also activates the `postgres` profile group
- H2 console is not available unless you explicitly run with the `local` profile

Default URL:

- `http://localhost:7777`

### Run with the current default profile

```bash
mvn spring-boot:run
```

Relevant config comes from:

- `src/main/resources/application.yml`
- `src/main/resources/application-postgres.yml`

PostgreSQL-related env vars:

- `POSTGRES_URL`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

JWT env vars:

- `APP_SECURITY_JWT_SECRET`
- `APP_SECURITY_JWT_SECRET_POSTGRES`

### Run in local H2 demo mode

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Local profile behavior:

- enables H2 console at `/h2-console`
- keeps the seeded course/demo flow easy to inspect
- uses `application-local.yml`

H2 console values:

- JDBC URL: `jdbc:h2:mem:elearningdb`
- Username: `sa`
- Password: blank

Example query:

```sql
SELECT e.id, u.full_name, u.email, c.title, e.status, e.enrolled_at
FROM enrollments e
JOIN platform_users u ON e.student_id = u.id
JOIN courses c ON e.course_id = c.id;
```

## Media storage configuration

Video uploads are configured under `app.media.video.*`.

Important settings:

- provider defaults to `s3`
- default endpoint is `http://localhost:9000`
- default public base URL is `http://localhost:9000/elearning-videos`
- allowed content types are `video/mp4` and `video/webm`
- default max file size is `536870912` bytes
- default presign duration is `15m`

The code is written for S3-compatible storage. Tests switch the provider to `in-memory`.

## Schema and persistence

- Flyway migrations in `src/main/resources/db/migration` are the schema source of truth
- JPA runs with `ddl-auto: validate`
- `docs/mvp-schema.sql` is now only a legacy reference and does not include the media-upload schema

## Current implementation notes

- the app seeds one course, `digital-skills-kz`, with two lessons if the database is empty
- lesson viewer responses include `videoUrl`
- anonymous enrollment can create a user shell that is later upgraded during registration with the same email
- `/api/admin/**` requires `ROLE_ADMIN`
- the repo currently contains integration and smoke tests for auth, enrollments, profile startup, and admin video upload behavior

## More project detail

For the AI-focused architecture summary and repo map, see `docs/PROJECT_BLUEPRINT.md`.
