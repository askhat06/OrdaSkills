# OrdaSkills — Technical Reference

> Backend: Spring Boot 3 · Java 21 · PostgreSQL 16  
> Port: **7777** · Base path: `/api`

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Architecture Overview](#architecture-overview)
3. [Security Model](#security-model)
4. [API Endpoints](#api-endpoints)
   - [Public](#public-no-auth)
   - [Authenticated (Student)](#authenticated-student)
   - [Teacher](#teacher-role-teacher)
   - [Admin](#admin-role-admin)
5. [Database Schema](#database-schema)
6. [Service Layer](#service-layer)
7. [Video Storage](#video-storage)
8. [Configuration Profiles](#configuration-profiles)
9. [Docker & CI/CD](#docker--cicd)
10. [Package Map](#package-map)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (Temurin JDK) |
| Framework | Spring Boot 3.5.11 |
| Security | Spring Security + JWT (JJWT 0.13.0, HS256) |
| ORM | Spring Data JPA / Hibernate |
| DB (production) | PostgreSQL 16 |
| DB (development) | H2 in-memory |
| Migrations | Flyway Core (10 versions) |
| Validation | Jakarta Bean Validation |
| Email | Spring Mail (JavaMailSender, SMTP) |
| Object Storage | Cloudflare R2 / MinIO (dev) |
| Build | Maven (multi-stage Docker build) |
| CI/CD | GitHub Actions → ghcr.io |
| Container | Docker (Alpine JRE 21, non-root `appuser`) |

---

## Architecture Overview

```
HTTP Request
    │
    ▼
JwtAuthenticationFilter ─── RequestRateLimitFilter
    │
    ▼
Controllers  (/api/auth, /api/courses, /api/teacher, /api/admin, …)
    │
    ▼
Services  (AuthService, CourseService, ProgressService, …)
    │
    ▼
Repositories  (Spring Data JPA)
    │
    ▼
Database  (PostgreSQL / H2)
```

**Pattern:** Layered monolith (Controller → Service → Repository → Entity).

**Cross-cutting concerns:**
- `AuditService` — compliance event logging on every admin mutating action
- `CourseOwnershipGuard` — ownership assertion reused across teacher endpoints
- `GlobalExceptionHandler` — maps domain exceptions to consistent JSON error responses

---

## Security Model

### Roles

| Role | Access |
|---|---|
| `STUDENT` | Public catalog, enrollments, progress, ratings |
| `TEACHER` | Own course/lesson CRUD, video upload, submit for review |
| `COMPANY` | Reserved |
| `ADMIN` | Full system; moderation, publish/reject courses |

**Admin provisioning:** The `ADMIN` role cannot be obtained through normal registration. On startup, `AdminSeeder` reads `ADMIN_EMAIL` and `ADMIN_PASSWORD` env vars and creates the admin account if it does not yet exist (idempotent). Set both env vars on the server before first deploy.

### JWT

- Algorithm: HS256, secret from env `APP_SECURITY_JWT_SECRET`
- Claims: `sub` (email), `role`, `fullName`, `iat`, `exp`
- Default expiry: 24 hours (configurable)
- Sent as `Authorization: Bearer <token>`

### Rate Limits (per IP, 1-minute window)

| Endpoint | Limit |
|---|---|
| `POST /api/auth/login` | 5 req/min |
| `POST /api/auth/register` | 3 req/min |
| `POST /api/enrollments` | 10 req/min |
| `GET /api/auth/verify` | 20 req/min |

Returns **429 Too Many Requests** when exceeded.

---

## API Endpoints

### Public (No Auth)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/health` | Health check |
| `POST` | `/api/auth/register` | Register new user |
| `GET` | `/api/auth/verify?token={token}` | Verify email address |
| `POST` | `/api/auth/login` | Login → JWT token |
| `GET` | `/api/courses` | Published course catalog |
| `GET` | `/api/courses/{slug}` | Course landing page |
| `GET` | `/api/courses/{courseSlug}/lessons/{lessonSlug}` | Lesson viewer |
| `POST` | `/api/enrollments` | Enroll in a course |

> Enrolled users retain access to `DRAFT`/`REJECTED` courses and lessons they were enrolled in before a status change.

---

### Authenticated (Student)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/auth/me` | Current user info (loads from DB — includes `location`, `avatarUrl`) |
| `PUT` | `/api/auth/me` | Update profile (`fullName` required; `location`, `avatarUrl` optional) |
| `GET` | `/api/enrollments` | List enrollments (filter: `?courseSlug=`, `?email=`) |
| `POST` | `/api/courses/{slug}/ratings` | Submit / update 1–5 star rating |
| `GET` | `/api/progress/courses/{courseSlug}` | Get course progress |
| `POST` | `/api/progress/courses/{courseSlug}/start` | Start course |
| `PUT` | `/api/progress/courses/{courseSlug}/current-step` | Update current lesson step |
| `POST` | `/api/progress/courses/{courseSlug}/steps/{lessonSlug}/complete` | Mark lesson complete |
| `POST` | `/api/progress/courses/{courseSlug}/complete` | Mark course complete |
| `POST` | `/api/progress/courses/{courseSlug}/reset` | Reset progress (retake) |

**`PUT /api/auth/me` request body:**
```json
{
  "fullName": "string (required, @NotBlank)",
  "location": "string | null | empty (optional)",
  "avatarUrl": "string | null | empty (optional)"
}
```
Returns `200` with updated user object. Email and role are not changeable via this endpoint.

---

### Teacher (Role: `TEACHER`)

#### Courses

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/teacher/courses` | All own courses (all statuses) |
| `GET` | `/api/teacher/courses/{courseSlug}` | Single owned course |
| `POST` | `/api/teacher/courses` | Create course → `DRAFT` |
| `PUT` | `/api/teacher/courses/{courseSlug}` | Update (`DRAFT` or `REJECTED` only) |
| `DELETE` | `/api/teacher/courses/{courseSlug}` | Delete `DRAFT` with no enrollments |
| `POST` | `/api/teacher/courses/{courseSlug}/submit` | Submit for review → `PENDING_REVIEW` |
| `POST` | `/api/teacher/courses/{courseSlug}/withdraw` | Withdraw → `DRAFT` |

#### Lessons

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/teacher/courses/{courseSlug}/lessons` | List lessons |
| `POST` | `/api/teacher/courses/{courseSlug}/lessons` | Create lesson (slug auto-generated from title) |
| `PUT` | `/api/teacher/courses/{courseSlug}/lessons/{lessonSlug}` | Update lesson |
| `DELETE` | `/api/teacher/courses/{courseSlug}/lessons/{lessonSlug}` | Delete lesson |
| `POST` | `/api/teacher/courses/{courseSlug}/lessons/{lessonSlug}/video-upload` | Initiate video upload |
| `POST` | `/api/teacher/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete` | Complete video upload |
| `DELETE` | `/api/teacher/courses/{courseSlug}/lessons/{lessonSlug}/video` | Remove video |

**Lesson creation request body:**
```json
{
  "title": "string (required)",
  "summary": "string (optional)",
  "content": "string (optional — plain text or JSON for assessments)",
  "videoUrl": "string (optional — external link)",
  "durationMinutes": "integer (optional, 1–600)"
}
```
Slug is always auto-generated server-side from `title`. Lessons can only be added to courses in `DRAFT` or `REJECTED` status.

**Video upload flow:**
1. `POST .../video-upload` with `{ "fileName", "contentType", "sizeBytes" }` → get `objectKey` + presigned `uploadUrl` + `requiredHeaders`
2. `PUT {uploadUrl}` from browser directly to MinIO/S3 with `Content-Type` from `requiredHeaders`
3. `POST .../video-upload/complete` with `{ "objectKey" }` → `204 No Content`; lesson now has `hasVideo: true`

**Video upload init response:**
```json
{
  "objectKey": "lessons/course-slug/lesson-slug/uuid-file.mp4",
  "uploadUrl": "https://minio.../presigned...",
  "requiredHeaders": { "Content-Type": "video/mp4" },
  "expiresAt": "2026-04-27T15:00:00Z",
  "playbackUrl": "https://cdn.oyan.ink/..."
}
```

---

### Admin (Role: `ADMIN`)

#### Course Moderation

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/admin/courses` | All courses (all statuses) |
| `GET` | `/api/admin/courses/pending` | Moderation queue (`PENDING_REVIEW`) |
| `GET` | `/api/admin/courses/{courseId}` | Course by ID |
| `POST` | `/api/admin/courses` | Create course |
| `PUT` | `/api/admin/courses/{courseId}` | Update course |
| `DELETE` | `/api/admin/courses/{courseId}` | Delete course |
| `POST` | `/api/admin/courses/{courseId}/publish` | Approve → `PUBLISHED` |
| `POST` | `/api/admin/courses/{courseId}/reject` | Reject with reason → `REJECTED` |

#### Lesson Video (Admin)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload` | Initiate upload |
| `POST` | `/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete` | Complete upload |
| `DELETE` | `/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video` | Remove video |

---

## Database Schema

### Course Status Workflow

```
DRAFT ──► PENDING_REVIEW ──► PUBLISHED
  ▲              │
  └──────────────┘ (withdraw)
                 │
                 ▼
              REJECTED ──► DRAFT (re-submit)
```

### Entities

| Table | Key Fields | Notes |
|---|---|---|
| `platform_users` | `email`, `role`, `is_lead`, `email_verified`, `location`, `avatar_url` | Verification token + expiry; location/avatar optional |
| `courses` | `slug`, `status`, `price`, `owner_id` | Owner nullable (admin-created) |
| `lessons` | `slug`, `position`, `video_url`, `video_storage_key`, `content` | `content` nullable (V10); max 8000 chars |
| `enrollments` | `course_id`, `student_id`, `status` | Unique (course, student) |
| `course_progress` | `course_id`, `student_id`, `percent_complete` | `attempt_count` for retakes |
| `course_progress_steps` | `progress_id`, `lesson_slug`, `step_order` | Per-lesson tracking |
| `course_ratings` | `course_id`, `student_id`, `rating` | 1–5 stars, unique per enrollment |
| `lesson_video_uploads` | `object_key`, `expires_at` | In-progress presigned upload state |
| `audit_events` | `actor_id`, `action`, `entity_type`, `payload` | JSON payload, append-only |

**All entities except `AuditEvent` extend `BaseEntity`** with `created_at` / `updated_at` timestamps.

### Migrations (Flyway)

| Version | Change |
|---|---|
| V1 | Initial schema (users, courses, lessons, enrollments) |
| V2 | Lesson video upload tracking |
| V3 | Course progress + progress steps |
| V4 | Teacher role + course status workflow |
| V5 | Email verification system |
| V6 | Course pricing + ratings |
| V7 | Lead tracking + verification index |
| V8 | Audit event logging |
| V9 | `location VARCHAR(255)`, `avatar_url VARCHAR(500)` on `platform_users` |
| V10 | `content` column on `lessons` made nullable |

---

## Service Layer

| Service | Responsibility |
|---|---|
| `AuthService` | Register, login, email verification, JWT generation, profile update |
| `JwtService` | Token creation and validation |
| `CourseService` | Public catalog and lesson viewer |
| `TeacherCourseService` | Teacher CRUD, submit/withdraw lifecycle |
| `AdminCourseService` | Admin CRUD, publish/reject |
| `TeacherLessonService` | Lesson CRUD (with ownership checks), slug auto-generation |
| `AdminLessonVideoService` | Video upload orchestration (shared by admin and teacher) |
| `EnrollmentService` | Enroll / list / status management |
| `ProgressService` | Progress lifecycle (start, step, complete, reset) |
| `CourseRatingService` | Rating submission and aggregation |
| `EmailService` | Sends verification emails |
| `AuditService` | Appends compliance events (`Propagation.MANDATORY`) |
| `CourseOwnershipGuard` | Asserts teacher owns the course (reusable) |
| `AdminSeeder` | Creates admin account on startup from env vars (all profiles, idempotent) |
| `DataSeeder` | Seeds dev course data on startup (`@Profile("local")` only) |

---

## Video Storage

**Abstraction:** `VideoStorageService` interface — implementation swapped per profile.

| Implementation | Profile | Notes |
|---|---|---|
| `S3VideoStorageService` | postgres | AWS SDK v2, presigned PUT + GET URLs |
| `InMemoryVideoStorageService` | local | No real storage, dev only |

**Constraints:**
- Max size: 512 MB (env `APP_MEDIA_VIDEO_MAX_FILE_SIZE_BYTES`, default `536870912`)
- Allowed MIME types: `video/mp4`, `video/webm`
- Presigned upload URL TTL: 2 hours (env `APP_MEDIA_VIDEO_PRESIGN_DURATION`)
- Single presigned PUT — no multipart

**Complete upload verification:** `/complete` does a HEAD request to S3 and validates that the actual `contentType` and `sizeBytes` match what was declared at init. Mismatch returns 500.

**CORS requirement:** MinIO/S3 must have a CORS rule allowing `PUT` from `https://www.oyan.ink` with `Content-Type` header, otherwise browser upload is blocked.

**Env vars for S3:**
```
APP_MEDIA_VIDEO_BUCKET
APP_MEDIA_VIDEO_ENDPOINT
APP_MEDIA_VIDEO_PUBLIC_BASE_URL
APP_MEDIA_VIDEO_ACCESS_KEY
APP_MEDIA_VIDEO_SECRET_KEY
APP_MEDIA_VIDEO_REGION          (default: us-east-1)
APP_MEDIA_VIDEO_LESSON_PREFIX   (default: lessons)
APP_MEDIA_VIDEO_PATH_STYLE_ACCESS_ENABLED
APP_MEDIA_VIDEO_PRESIGN_DURATION (default: 2h)
```

---

## Configuration Profiles

`application-prod.yml` has been removed — its contents were merged into `application-postgres.yml`.

| Profile | Database | Email | Video Storage |
|---|---|---|---|
| `local` | H2 in-memory | Disabled | In-memory |
| `postgres` (default) | PostgreSQL | SMTP enabled | S3 / MinIO |

**Key env vars:**

```bash
# Security
APP_SECURITY_JWT_SECRET=<min 32 chars, base64>

# Admin account (provisioned on first startup)
ADMIN_EMAIL=admin@oyan.ink
ADMIN_PASSWORD=<strong password>

# Database
POSTGRES_URL=jdbc:postgresql://host:5432/db
POSTGRES_USER=...
POSTGRES_PASSWORD=...

# Email
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=...
MAIL_PASSWORD=...
APP_BASE_URL=https://yourfrontend.com
EMAIL_TOKEN_EXPIRY=24       # hours, default 24

# Video storage (S3 / MinIO)
APP_MEDIA_VIDEO_BUCKET=...
APP_MEDIA_VIDEO_ENDPOINT=...
APP_MEDIA_VIDEO_PUBLIC_BASE_URL=...
APP_MEDIA_VIDEO_ACCESS_KEY=...
APP_MEDIA_VIDEO_SECRET_KEY=...
```

---

## Docker & CI/CD

### docker-compose Stack

| Service | Image | Port |
|---|---|---|
| `db` | postgres:16-alpine | 5432 |
| `minio` | minio/minio | 9000 (API), 9001 (console) |
| `minio-init` | minio/mc | — (one-shot bucket init) |
| `app` | built from repo | 7777 |

### Dockerfile (multi-stage)

- **Stage 1 (build):** `eclipse-temurin:21-jdk-alpine` + Maven → fat JAR
- **Stage 2 (runtime):** `eclipse-temurin:21-jre-alpine` + non-root `appuser`
  - JVM: 75% container memory, `/dev/./urandom` entropy
  - Health check: `GET /api/health`

### GitHub Actions

| Job | Trigger | Action |
|---|---|---|
| `test` | All PRs & pushes | `mvn test` with H2 |
| `build-push` | `main` branch & version tags | Build & push to `ghcr.io` |

Image tags: branch name, semver patterns, short SHA.

---

## Package Map

```
kz.skills.elearning
├── config/           SecurityConfig, WebConfig, RateLimitProperties,
│                     VideoStorageConfig, VideoStorageProperties
├── controller/       AuthController, CourseController, EnrollmentController,
│                     ProgressController, CourseRatingController,
│                     TeacherCourseController, TeacherLessonController,
│                     AdminCourseController, AdminLessonVideoController,
│                     AppInfoController
├── service/          AuthService, CourseService, TeacherCourseService,
│                     AdminCourseService, TeacherLessonService,
│                     AdminLessonVideoService, EnrollmentService,
│                     ProgressService, CourseRatingService,
│                     EmailService, AuditService, CourseOwnershipGuard,
│                     AdminSeeder, DataSeeder
├── service/video/    VideoStorageService (interface), S3VideoStorageService,
│                     InMemoryVideoStorageService, VideoUploadSession,
│                     PendingVideoUpload, StoredVideoObject
├── security/         JwtService, JwtAuthenticationFilter,
│                     PlatformUserDetailsService, PlatformUserPrincipal,
│                     RequestRateLimitFilter
├── entity/           PlatformUser, Course, Lesson, Enrollment,
│                     CourseProgress, CourseProgressStep, CourseRating,
│                     LessonVideoUpload, AuditEvent, BaseEntity
│                     Enums: UserRole, CourseStatus, EnrollmentStatus,
│                           ProgressStatus, ProgressStepStatus
├── repository/       PlatformUserRepository, CourseRepository,
│                     LessonRepository, EnrollmentRepository,
│                     CourseProgressRepository, CourseRatingRepository,
│                     LessonVideoUploadRepository, AuditEventRepository
├── dto/              Auth (AuthResponse, LoginRequest, RegisterRequest,
│                          UpdateProfileRequest, CurrentUserResponse),
│                     Course, Lesson (TeacherLessonRequest/Response),
│                     Enrollment, Progress, Video (AdminVideoUpload*),
│                     Rating, Moderation, common (MessageResponse, ApiErrorResponse)
├── exception/        GlobalExceptionHandler + domain exceptions
│                     (BadRequest, NotFound, Conflict, DuplicateEnrollment,
│                      InvalidCredentials, EmailNotVerified, VideoUpload)
└── util/             SlugUtils
```
