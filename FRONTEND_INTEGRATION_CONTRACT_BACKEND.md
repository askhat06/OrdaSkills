# Frontend Integration Contract: Backend Source of Truth

Audit date: `2026-04-22`

Repository role: Spring Boot backend only

Verification status:
- Confirmed from backend code, DTOs, controllers, security config, YAML profiles, Flyway migrations, and checked-in tests.
- Checked-in tests were inspected as source material.
- Targeted progress integration coverage was re-run in this workspace.

Contract precedence for this project:
1. Controller mappings
2. Request/response DTOs
3. Service logic
4. Security config and security filters
5. Active YAML/profile configuration
6. Flyway migrations and entity mappings
7. Integration tests
8. Older docs

If this document disagrees with older docs, backend code wins.

## 1. Project Scope and Repository Boundary

This repository is the backend repository for the eLearning product.

This repository does contain:
- Spring Boot HTTP controllers
- request and response DTOs
- business rules for auth, enrollment, courses, admin course CRUD, and admin lesson video flows
- JWT auth and authorization rules
- rate limiting
- profile and environment configuration
- Flyway schema migrations
- backend tests

This repository does not contain:
- the canonical React SPA frontend
- frontend routes, state containers, or UI code
- frontend upload helpers
- legacy `json-server` frontend code

Frontend ownership belongs in the separate frontend repository. Backend changes here must be documented in a way that a frontend agent can consume without assuming any frontend files exist in this workspace.

Frontend assumptions must be revalidated in the frontend repository when they depend on:
- current SPA route layout
- current token storage mechanism
- current component ownership
- current legacy `json-server` integration points
- current admin UI structure

eLearning domain behavior must follow this backend contract. Legacy non-eLearning domains may still use `json-server`, but auth, courses, lessons, enrollments, and admin eLearning flows must be aligned to this backend.

## 2. Contract Source of Truth

### 2.1 Real contract-defining classes and files

| File | Purpose | Frontend significance |
| --- | --- | --- |
| `src/main/java/kz/skills/elearning/controller/AuthController.java` | Auth routes | Exact auth paths and statuses |
| `src/main/java/kz/skills/elearning/controller/CourseController.java` | Public course and lesson routes | Public catalog, landing, and lesson viewer surface |
| `src/main/java/kz/skills/elearning/controller/EnrollmentController.java` | Enrollment create/list routes | Public enrollment plus authenticated enrollment lookup |
| `src/main/java/kz/skills/elearning/controller/ProgressController.java` | Authenticated progress routes | Exact progress paths, auth requirement, and statuses |
| `src/main/java/kz/skills/elearning/controller/AdminCourseController.java` | Admin course CRUD | Admin route contract and `courseId` usage |
| `src/main/java/kz/skills/elearning/controller/AdminLessonVideoController.java` | Admin video routes | Upload-init, upload-complete, delete-video contract |
| `src/main/java/kz/skills/elearning/dto/*.java` | HTTP payload shapes | Exact request and response fields |
| `src/main/java/kz/skills/elearning/service/AuthService.java` | Register/login/current-user logic | Email normalization, lead upgrade, generic login errors |
| `src/main/java/kz/skills/elearning/service/EnrollmentService.java` | Enrollment rules | Anonymous enrollment, lead-shell behavior, admin vs learner enrollment listing, progress bootstrap |
| `src/main/java/kz/skills/elearning/service/ProgressService.java` | Course progress lifecycle | Start/reset/complete semantics, percent calculation, lesson-step sync |
| `src/main/java/kz/skills/elearning/service/AdminCourseService.java` | Admin CRUD rules | Slug normalization, delete conflict behavior |
| `src/main/java/kz/skills/elearning/service/AdminLessonVideoService.java` | Video workflow rules | Single pending upload, size/content-type verification, completion semantics |
| `src/main/java/kz/skills/elearning/entity/CourseProgress.java` | Aggregate progress state | Source of truth for attempts, counters, timestamps, and current step |
| `src/main/java/kz/skills/elearning/entity/CourseProgressStep.java` | Step-level progress state | Explicit per-lesson completion model |
| `src/main/java/kz/skills/elearning/config/SecurityConfig.java` | Route authorization and JSON `401`/`403` behavior | Public vs authenticated vs admin-only truth |
| `src/main/java/kz/skills/elearning/security/JwtAuthenticationFilter.java` | JWT parsing and DB-backed principal loading | Malformed/deleted-user token behavior |
| `src/main/java/kz/skills/elearning/security/RequestRateLimitFilter.java` | Public POST rate limiting | `429` contract |
| `src/main/resources/application.yml` | Shared defaults | Port, default profile, CORS, JWT expiry, rate-limit defaults |
| `src/main/resources/application-local.yml` | Local profile behavior | H2, H2 console, in-memory media, local JWT secret |
| `src/main/resources/application-postgres.yml` | Non-local storage/runtime behavior | PostgreSQL, S3, required env variables |
| `src/main/resources/application-prod.yml` | Production overlay | Disables H2 console |
| `src/main/resources/db/migration/V1__init_schema.sql` | Canonical base schema | Tables and uniqueness constraints |
| `src/main/resources/db/migration/V2__add_lesson_video_uploads.sql` | Video schema extension | Lesson video metadata and pending upload table |
| `src/main/resources/db/migration/V3__add_course_progress_tracking.sql` | Progress schema extension | `course_progress` and `course_progress_steps` contract |
| `src/test/java/kz/skills/elearning/ApiIntegrationTests.java` | Integration contract tests | Verifies route behavior and edge cases |
| `src/test/java/kz/skills/elearning/RateLimitingIntegrationTests.java` | Rate-limit tests | Verifies `429` behavior |
| `src/test/java/kz/skills/elearning/ProfileConfigurationTests.java` | Fail-closed config test | Verifies non-local JWT secret requirement |

### 2.2 Important stale or secondary files

| File | Status | Why it is not final truth |
| --- | --- | --- |
| `docs/PROJECT_BLUEPRINT.md` | Helpful secondary context | Contains useful guidance, but code still wins |
| `docs/REQUIREMENTS_MATRIX.md` | Helpful secondary context | Requirement tracking, not endpoint contract authority |
| `docs/mvp-schema.sql` | Stale for schema contract | Does not include lesson video upload and progress schema added in `V2__add_lesson_video_uploads.sql` and `V3__add_course_progress_tracking.sql` |

## 3. Implemented Backend Capabilities

### 3.1 Confirmed implemented features

- Auth registration via `POST /api/auth/register` (may return `MessageResponse` when email verification is required)
- Email verification via `GET /api/auth/verify?token=`
- Auth login via `POST /api/auth/login`
- Session restoration/current-user lookup via `GET /api/auth/me`
- Public course catalog via `GET /api/courses`
- Public course landing via `GET /api/courses/{slug}`
- Public lesson viewer via `GET /api/courses/{courseSlug}/lessons/{lessonSlug}`
- Public enrollment creation via `POST /api/enrollments`
- Authenticated enrollment listing via `GET /api/enrollments`
- Authenticated course rating via `POST /api/courses/{slug}/ratings`
- Authenticated course progress retrieval, start, update, completion, and reset via `/api/progress/courses/**`
- Teacher course CRUD and lifecycle transitions via `/api/teacher/courses/**`
- Teacher lesson CRUD and video upload via `/api/teacher/courses/{courseSlug}/lessons/**`
- Admin course list/get/create/update/delete via `/api/admin/courses`
- Admin moderation queue via `GET /api/admin/courses/pending`
- Admin course approve/reject via `POST /api/admin/courses/{courseId}/publish` and `/reject`
- Admin lesson video upload-init, upload-complete, and delete via `/api/admin/courses/{courseSlug}/lessons/{lessonSlug}`
- Health check via `GET /api/health`
- Rate limiting for `POST /api/auth/login`, `POST /api/auth/register`, and `POST /api/enrollments`
- Automatic progress bootstrap for new enrollments
- Explicit per-lesson progress tracking with attempt history
- Role-based access control with `ROLE_STUDENT`, `ROLE_TEACHER`, `ROLE_COMPANY`, `ROLE_ADMIN`
- Course status workflow: DRAFT → PENDING_REVIEW → PUBLISHED / REJECTED
- Local default profile with H2 and in-memory media configuration
- PostgreSQL/S3 profile path with required env vars
- Startup data seeding when the course table is empty

### 3.2 Confirmed backend behaviors designed to reduce frontend conflicts

- Security returns JSON bodies for `401` and `403` instead of default HTML/error pages.
- JWT auth does not trust token claims alone. The filter reloads the user from the database on each authenticated request. Deleted users therefore fall back to `401` instead of creating stale ghost sessions.
- Login uses a generic `401 Invalid email or password` response and does not reveal whether the email belongs to a passwordless lead-shell account.
- Enrollment creation returns explicit `409` on duplicate enrollment instead of silently succeeding.
- Enrollment on an existing passwordless lead-shell account does not overwrite nonblank `fullName` or `locale`.
- Registration upgrades an existing passwordless lead-shell user instead of forcing a separate migration path.
- Course progress is stored as backend state. `percentComplete` is recalculated on the server from explicit persisted step rows.
- Enrollment initializes course progress in `NOT_STARTED`, and enrolled users can later resume with authenticated progress endpoints.
- Progress steps are synchronized against the current lesson set for a course, so added lessons appear as new `NOT_STARTED` steps and removed lessons are pruned.
- Duplicate step-complete/start calls do not double-count completed steps or attempts.
- Admin course deletion returns explicit `409` if enrollments exist, instead of deleting related learner data implicitly.
- Admin video upload keeps only one pending upload per lesson. Starting a new upload invalidates the previous pending upload row.

### 3.3 Confirmed absences and non-features

These are important because frontend code must not assume they exist.

- No refresh-token endpoint
- No logout endpoint
- No cookie/session auth flow
- No assessments API
- No localized content API beyond plain `locale` strings on records
- No admin lesson list endpoint (use teacher or public course landing endpoint for lesson data)
- No pagination or total-count wrapper on list endpoints
- No backend media proxy route for serving uploaded video files
- No backend endpoint that publishes allowed upload content types or max file size to the frontend at runtime
- No admin self-service account creation endpoint
- No GET endpoint for individual course ratings

## 4. Endpoint-by-Endpoint API Contract

### 4.0 Shared response DTO shapes

#### `AuthResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `accessToken` | string | JWT access token |
| `tokenType` | string | Always `"Bearer"` |
| `expiresInSeconds` | number | Default `86400` seconds from config |
| `user` | object | `CurrentUserResponse` |

#### `CurrentUserResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `id` | number | Platform user ID |
| `fullName` | string | Stored user name |
| `email` | string | Lowercased during register/login lookup |
| `locale` | string | Casing is not globally consistent across all flows |
| `role` | string | `STUDENT`, `TEACHER`, `COMPANY`, or `ADMIN` |

#### `CourseSummaryResponse`

List endpoint returns a raw array of these objects.

| Field | Type | Notes |
| --- | --- | --- |
| `id` | number | Course ID |
| `slug` | string | Canonical public slug |
| `title` | string | Course title |
| `subtitle` | string or `null` | Optional |
| `locale` | string | Course locale |
| `level` | string or `null` | Optional |
| `durationHours` | number or `null` | Nullable at entity level |
| `lessonCount` | number | Count of attached lessons |
| `instructorName` | string or `null` | Optional |
| `enrollmentCount` | number | Total number of enrollments |
| `price` | number or `null` | `null` means the course is free |
| `averageRating` | number or `null` | Average star rating, `null` if no ratings yet |
| `ratingCount` | number | Total number of ratings submitted |

#### `CourseLandingResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `slug` | string | Canonical course slug |
| `title` | string | Course title |
| `subtitle` | string or `null` | Optional |
| `description` | string | Course body |
| `locale` | string | Course locale |
| `instructorName` | string or `null` | Optional |
| `level` | string or `null` | Optional |
| `durationHours` | number or `null` | Nullable at entity level |
| `lessons` | array | Sorted by `position` ascending |

#### `LessonOutlineResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `slug` | string | Lesson slug unique within course |
| `title` | string | Lesson title |
| `position` | number | Sort/display order |
| `durationMinutes` | number or `null` | Optional |
| `summary` | string or `null` | Optional |

#### `LessonViewerResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `courseSlug` | string | Course slug |
| `courseTitle` | string | Course title |
| `lessonSlug` | string | Lesson slug |
| `lessonTitle` | string | Lesson title |
| `position` | number | Lesson order |
| `durationMinutes` | number or `null` | Optional |
| `videoUrl` | string or `null` | Nullable, especially after admin delete or before upload |
| `content` | string | Lesson content body |

#### `EnrollmentResponse`

List endpoint returns a raw array of these objects.

| Field | Type | Notes |
| --- | --- | --- |
| `enrollmentId` | number | Enrollment ID |
| `studentId` | number | Platform user ID |
| `studentName` | string | Stored user name |
| `email` | string | Lowercased email |
| `locale` | string | Stored locale |
| `courseSlug` | string | Course slug |
| `courseTitle` | string | Course title |
| `status` | string | Currently `ENROLLED` on create |
| `enrolledAt` | string | Serialized `LocalDateTime`, no timezone offset in payload |

#### `CourseProgressStepResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `lessonSlug` | string | Lesson slug for this progress step |
| `lessonTitle` | string | Canonical lesson title |
| `position` | number | Step order aligned to lesson `position` |
| `status` | string | `NOT_STARTED` or `COMPLETED` |
| `completedAt` | string or `null` | Serialized `LocalDateTime`, no timezone offset in payload |

#### `CourseProgressResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `userId` | number | Platform user ID |
| `courseId` | number | Numeric course ID |
| `courseSlug` | string | Canonical course slug |
| `courseTitle` | string | Course title |
| `status` | string | `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, or `RESET` |
| `currentStep` | string or `null` | Current lesson slug or `null` when inactive/completed |
| `completedSteps` | number | Count of completed persisted steps |
| `totalSteps` | number | Count of current lesson-backed steps |
| `percentComplete` | number | Backend-computed percentage in the `0..100` range |
| `attemptCount` | number | Increments when a new attempt starts from `NOT_STARTED` or `RESET` |
| `startedAt` | string or `null` | Serialized `LocalDateTime`, no timezone offset in payload |
| `updatedAt` | string | Serialized `LocalDateTime`, no timezone offset in payload |
| `completedAt` | string or `null` | Serialized `LocalDateTime`, no timezone offset in payload |
| `resetAt` | string or `null` | Serialized `LocalDateTime`, no timezone offset in payload |
| `steps` | array | Raw array of `CourseProgressStepResponse`, sorted by lesson order |

#### `AdminCourseResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `id` | number | Numeric admin identifier for course CRUD |
| `slug` | string | Canonical slug |
| `title` | string | Title |
| `subtitle` | string or `null` | Optional |
| `description` | string | Description |
| `locale` | string | Lowercased during create/update |
| `instructorName` | string or `null` | Optional |
| `level` | string or `null` | Optional |
| `durationHours` | number | Required in admin DTO |
| `lessonCount` | number | Count of lessons currently attached |
| `status` | string | `DRAFT`, `PENDING_REVIEW`, `PUBLISHED`, or `REJECTED` |
| `ownerEmail` | string | Email of the teacher who owns the course |

#### `TeacherCourseResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `id` | number | Course ID |
| `slug` | string | Canonical slug |
| `title` | string | Title |
| `subtitle` | string or `null` | Optional |
| `description` | string | Description |
| `locale` | string | Course locale |
| `level` | string or `null` | Optional |
| `durationHours` | number | Duration in hours |
| `price` | number or `null` | `null` means free |
| `status` | string | `DRAFT`, `PENDING_REVIEW`, `PUBLISHED`, or `REJECTED` |
| `rejectionReason` | string or `null` | Admin rejection feedback; `null` unless `status == REJECTED` |
| `lessonCount` | number | Count of lessons currently attached |
| `createdAt` | string | Serialized `LocalDateTime`, no timezone offset |
| `updatedAt` | string | Serialized `LocalDateTime`, no timezone offset |

#### `TeacherLessonResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `id` | number | Lesson ID |
| `courseSlug` | string | Owning course slug |
| `slug` | string | Lesson slug (unique within the course) |
| `title` | string | Lesson title |
| `summary` | string or `null` | Optional short description |
| `content` | string | Lesson body |
| `position` | number | Lesson ordering |
| `durationMinutes` | number or `null` | Optional |
| `videoUrl` | string or `null` | Nullable; set after video upload or direct URL |
| `hasVideo` | boolean | Convenience flag, true when `videoUrl` is not null |
| `createdAt` | string | Serialized `LocalDateTime`, no timezone offset |
| `updatedAt` | string | Serialized `LocalDateTime`, no timezone offset |

#### `MessageResponse`

Returned by `POST /api/auth/register` when email verification is required.

| Field | Type | Notes |
| --- | --- | --- |
| `message` | string | Human-readable status message |

#### `AdminVideoUploadInitResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `objectKey` | string | Server-generated storage key |
| `uploadUrl` | string | Storage-provider upload target, not guaranteed to be backend HTTP |
| `requiredHeaders` | object | Must be sent on direct upload |
| `expiresAt` | string | Serialized `Instant`, includes timezone/offset |
| `playbackUrl` | string | Predicted playback URL after successful completion |

#### `ApiErrorResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `timestamp` | string | Serialized `LocalDateTime`, no timezone offset in payload |
| `status` | number | HTTP status |
| `error` | string | HTTP reason phrase |
| `message` | string | Human-readable message |
| `validationErrors` | object | Field-to-message map, often `{}` when not a validation failure |

### 4.1 `GET /api/health`

- Auth requirement: Public
- Purpose: Liveness and connectivity check
- Request body: none
- Success response: JSON object with `status` and `service`
- Important statuses:
  - `200` with body `{ "status": "ok", "service": "elearning-backend" }`
- Frontend handling notes:
  - Use only for health/liveness checks.
  - Do not use this route for feature detection, auth detection, or version detection.
- Conflict risk:
  - Frontend code that expects build/version metadata will break; this endpoint does not provide it.

### 4.2 `POST /api/auth/register`

- Auth requirement: Public
- Purpose: Create a registered account or upgrade an existing passwordless lead-shell user to a registered account
- Request JSON:

| Field | Type | Required | Constraints | Notes |
| --- | --- | --- | --- | --- |
| `fullName` | string | yes | not blank, max `120` | Trimmed before save |
| `email` | string | yes | valid email, max `120` | Lowercased before lookup/save |
| `password` | string | yes | not blank, length `8..72` | BCrypt encoded |
| `locale` | string | no | max `10` | Defaults to `"ru"` if null/blank, lowercased on save |

- Success response (two variants):
  - `201 Created` with body `AuthResponse` when email verification is **not** required
  - `201 Created` with body `MessageResponse` when email verification **is** required — the user must verify before logging in
- Important statuses:
  - `201` on successful registration or successful lead-shell upgrade
  - `400` on validation failure
  - `409` if the email already belongs to a user with a nonblank password hash
  - `429` if registration rate limit is exceeded
- Frontend handling notes:
  - Check whether the response body is `AuthResponse` (contains `accessToken`) or `MessageResponse` (contains only `message`). When `MessageResponse` is returned, do not attempt to store a token — direct the user to verify their email.
  - Treat the response `user` object in `AuthResponse` as canonical. It contains normalized values.
  - Store `accessToken`, use `tokenType` as `"Bearer"`, and treat `expiresInSeconds` as the configured JWT lifetime hint.
  - If registration succeeds for an email that already enrolled anonymously, that is expected backend behavior.
- Conflict risks if frontend assumes the wrong contract:
  - If the frontend always expects `AuthResponse`, it will crash or produce stale state when email verification returns `MessageResponse` instead.
  - If the frontend assumes every existing email returns `409`, it will block valid lead-shell upgrades.
  - If the frontend omits `locale`, the backend will silently default it to `"ru"`.
  - If the frontend allows register-email lengths above `120`, it may create an enrollment that can never be upgraded through this route.

### 4.2a `GET /api/auth/verify`

- Auth requirement: Public
- Purpose: Verify the user's email address using the one-time token sent in the registration email
- Query parameter:
  - `token`: the verification token from the email link
- Success response:
  - `200 OK`
  - Body is `AuthResponse`
- Important statuses:
  - `200` on valid, unexpired token — the account is now active and the user is logged in
  - `400` when the token is invalid, expired, or already used
- Frontend handling notes:
  - After the user clicks the email link, call this endpoint with the `token` query param.
  - On `200`, store the returned `accessToken` and hydrate session state — the user is now authenticated.
  - On `400`, show an error and offer a path to re-register or request a new verification email if that feature exists.

### 4.3 `POST /api/auth/login`

- Auth requirement: Public
- Purpose: Exchange credentials for a JWT access token
- Request JSON:

| Field | Type | Required | Constraints | Notes |
| --- | --- | --- | --- | --- |
| `email` | string | yes | valid email | Lowercased before lookup |
| `password` | string | yes | not blank | Checked through Spring Security authentication |

- Success response:
  - `200 OK`
  - Body is `AuthResponse`
- Important statuses:
  - `200` on success
  - `400` on validation failure
  - `401` with message `Invalid email or password` for bad credentials and for passwordless lead-shell accounts
  - `429` if login rate limit is exceeded
- Frontend handling notes:
  - Do not infer account existence, lead-shell status, or "register first" status from login failures.
  - Store the returned token exactly as returned.
- Conflict risks:
  - Frontend code that branches on login error message to determine whether the user must register is not aligned with backend behavior.
  - Successful and failed attempts both count toward the rate limit.

### 4.4 `GET /api/auth/me`

- Auth requirement: Authenticated
- Purpose: Return the current authenticated user snapshot
- Request body: none
- Required header: `Authorization: Bearer <accessToken>`
- Success response:
  - `200 OK`
  - Body is `CurrentUserResponse`
- Important statuses:
  - `200` on valid authenticated session
  - `401` with message `Authentication required` when token is missing, malformed, expired, invalid, or belongs to a deleted user
- Frontend handling notes:
  - Use this route to restore session state on app startup when a token is present.
  - On `401`, clear the stored token and all authenticated session state immediately.
  - Role in this response is more authoritative than a stale client-decoded JWT, because the backend reloads the user from the database.
- Conflict risks:
  - Do not assume a token remains valid just because it has not reached the client-side expiry timer.
  - Do not keep a deleted-user or role-changed session alive on the client after `401`.

### 4.5 `GET /api/courses`

- Auth requirement: Public
- Purpose: Return the public course catalog (only PUBLISHED courses are visible here)
- Request body: none
- Success response:
  - `200 OK`
  - Body is a raw JSON array of `CourseSummaryResponse`
- Important statuses:
  - `200`
- Confirmed ordering:
  - Sorted by `createdAt` descending
- Frontend handling notes:
  - There is no pagination wrapper and no metadata object.
  - Use `slug` from this payload as the canonical public course identifier.
  - `price` is `null` for free courses. `averageRating` is `null` if no ratings have been submitted.
- Conflict risks:
  - Frontend code expecting `items`, `data`, or `pagination` wrappers will break.
  - Frontend code expecting the list to be alphabetical or stable by ID will not match backend ordering.
  - DRAFT, PENDING_REVIEW, and REJECTED courses are not returned here.

### 4.6 `GET /api/courses/{slug}`

- Auth requirement: Public
- Purpose: Return the public landing payload for a single course
- Path parameter:
  - `slug`: exact course slug
- Success response:
  - `200 OK`
  - Body is `CourseLandingResponse`
- Important statuses:
  - `200`
  - `404` with message `Course not found: {slug}`
- Confirmed ordering:
  - `lessons` are sorted by `position` ascending in the service layer
- Frontend handling notes:
  - This payload does not include learner enrollment state.
  - Optional fields may be `null`.
- Conflict risks:
  - Do not assume the course landing payload contains course ID.
  - Do not assume enrollment status, progress, or gating flags exist here.
  - Do not rewrite or uppercase slugs before sending them back to the backend.

### 4.7 `GET /api/courses/{courseSlug}/lessons/{lessonSlug}`

- Auth requirement: Public
- Purpose: Return the public lesson viewer payload
- Path parameters:
  - `courseSlug`: exact course slug
  - `lessonSlug`: exact lesson slug
- Success response:
  - `200 OK`
  - Body is `LessonViewerResponse`
- Important statuses:
  - `200`
  - `404` with message `Lesson not found: {lessonSlug} for course: {courseSlug}`
- Frontend handling notes:
  - `videoUrl` may be `null`.
  - This route is public. Backend does not require authentication or enrollment to read the lesson viewer payload.
- Conflict risks:
  - Frontend route guards that require auth for lesson viewing are stricter than the current backend contract.
  - Frontend code that assumes `videoUrl` is always present will break after admin delete or before upload.

### 4.8 `POST /api/enrollments`

- Auth requirement: Public
- Purpose: Create an enrollment and create or reuse a learner record
- Request JSON:

| Field | Type | Required | Constraints | Notes |
| --- | --- | --- | --- | --- |
| `courseSlug` | string | yes | not blank | Exact slug lookup, no service-layer normalization |
| `fullName` | string | yes | not blank, max `120` | Trimmed before persistence |
| `email` | string | yes | valid email, max `180` | Lowercased before lookup/save |
| `locale` | string | yes | not blank, max `20` | Trimmed before persistence, not lowercased here |

- Success response:
  - `201 Created`
  - Body is `EnrollmentResponse`
- Important statuses:
  - `201` on successful enrollment
  - `400` on validation failure
  - `404` when `courseSlug` does not match a course
  - `409` when the student is already enrolled in that course
  - `429` if enrollment rate limit is exceeded
- Confirmed behavior:
  - Enrollment is anonymous-friendly. No JWT is required.
  - If no user exists for the email, the backend creates a passwordless learner record.
  - If a learner exists for the email and has no password, the backend reuses that learner.
  - If that existing learner already has nonblank `fullName` or `locale`, those fields are not overwritten.
  - Successful enrollment bootstraps course progress in `NOT_STARTED` with explicit lesson-backed step rows.
- Frontend handling notes:
  - Do not force registration before allowing enrollment.
  - Use `409` to show "already enrolled" messaging rather than a generic failure.
  - Use the response as confirmation of the enrollment record that was actually created.
  - Enrollment can succeed before the learner registers, but progress routes still require authenticated access later.
- Conflict risks:
  - `courseSlug` is looked up exactly. Frontend code that injects whitespace or noncanonical casing can cause `404`.
  - Enrollment allows email length up to `180`, but registration allows only `120`. If the frontend allows `121..180` here, later register-upgrade can fail validation.

### 4.9 `GET /api/enrollments`

- Auth requirement: Authenticated
- Purpose: List enrollments for the current learner or, for admins, query enrollments across users
- Query parameters:

| Param | Type | Required | Notes |
| --- | --- | --- | --- |
| `courseSlug` | string | no | Exact course-slug filter |
| `email` | string | no | Admin filter, or learner self-filter only |

- Success response:
  - `200 OK`
  - Body is a raw JSON array of `EnrollmentResponse`
- Important statuses:
  - `200`
  - `401` when unauthenticated
  - `403` when a learner tries to query another learner's email
- Confirmed role behavior:
  - `ADMIN` can query all enrollments, optionally filtered by `courseSlug`, `email`, or both.
  - `STUDENT` can only view their own enrollments.
  - Student requests with `email` pointing at another learner return `403` with message `Students can only view their own enrollments`.
- Confirmed ordering:
  - Sorted by `enrolledAt` descending
- Frontend handling notes:
  - Empty arrays are valid successful responses.
  - There is no pagination or metadata wrapper.
  - This endpoint does not embed course-progress snapshots. Progress is fetched separately under `/api/progress/courses/**`.
- Conflict risks:
  - Frontend code that lets learners query arbitrary emails will produce backend `403`.
  - Frontend code that expects admin-only access here is too strict; regular learners can call it for themselves.

### 4.9a `POST /api/courses/{slug}/ratings`

- Auth requirement: Authenticated and enrolled in the target course
- Purpose: Submit or update a star rating (1–5) for a course
- Path parameter:
  - `slug`: exact course slug
- Request JSON:

| Field | Type | Required | Constraints | Notes |
| --- | --- | --- | --- | --- |
| `rating` | number | yes | integer `1..5` | Replaces any existing rating by this user for this course |

- Success response:
  - `204 No Content`
- Important statuses:
  - `204` on successful submit or update
  - `400` on validation failure (rating outside 1–5)
  - `401` when unauthenticated
  - `403` when authenticated but not enrolled
  - `404` when course slug does not exist
- Frontend handling notes:
  - This is an upsert — calling it again with a different value replaces the previous rating.
  - Do not parse JSON on `204`.
  - Refetch `GET /api/courses` or `GET /api/courses/{slug}` if the UI needs the updated `averageRating`.
- Conflict risks:
  - Frontend code that does not check enrollment status before showing the rating widget may produce `403`.

### 4.10 Progress endpoints under `/api/progress/courses/{courseSlug}`

- Auth requirement: Authenticated and enrolled in the target course
- Purpose: Read and mutate backend-owned course progress
- Common success response:
  - `200 OK`
  - Body is `CourseProgressResponse`
- Common statuses:
  - `200`
  - `400` with message `User is not enrolled in course: {courseSlug}` when the authenticated learner is not enrolled
  - `401` when unauthenticated
  - `404` when the course slug or lesson slug does not exist for route variants that reference them
- Shared backend invariants:
  - Progress is initialized on enrollment and can be recreated lazily for already-enrolled users if the row is missing.
  - `percentComplete` is always recalculated on the backend from persisted step rows.
  - Step rows are synchronized to the current lesson set for the course.
  - `currentStep` can be `null` for `NOT_STARTED`, `RESET`, and `COMPLETED` states.

Supported routes:

- `GET /api/progress/courses/{courseSlug}`
  - Returns the current progress snapshot for the enrolled learner.
  - If progress has never been started, the snapshot can still exist in `NOT_STARTED`.

- `POST /api/progress/courses/{courseSlug}/start`
  - Starts or resumes a learner attempt.
  - Transitions `NOT_STARTED` or `RESET` to `IN_PROGRESS`.
  - Increments `attemptCount` only when a new attempt actually begins.
  - Sets `currentStep` to the next incomplete lesson when one exists.

- `PUT /api/progress/courses/{courseSlug}/current-step`
  - Request body: `{ "lessonSlug": "..." }`
  - Updates the current active lesson without marking it complete.
  - Returns the full canonical `CourseProgressResponse`.

- `POST /api/progress/courses/{courseSlug}/steps/{lessonSlug}/complete`
  - Idempotently marks a lesson step as completed.
  - Recalculates `completedSteps`, `totalSteps`, and `percentComplete`.
  - Advances `currentStep` to the next incomplete lesson or clears it on completion.

- `POST /api/progress/courses/{courseSlug}/complete`
  - Marks every current step as completed in one backend operation.
  - If `totalSteps == 0`, the response still becomes `COMPLETED` with `percentComplete = 100`.

- `POST /api/progress/courses/{courseSlug}/reset`
  - Clears step completion timestamps and sets every step back to `NOT_STARTED`.
  - Sets aggregate status to `RESET`.
  - Does not increment `attemptCount`; the next `/start` call begins the next attempt.

- Frontend handling notes:
  - Do not compute learner progress on the client when backend progress is available.
  - Do not assume opening the public lesson viewer automatically mutates progress; progress changes only through progress routes.
  - Render backend `steps` as the source of truth for completion state and ordering.
  - Treat `400` not-enrolled responses as a domain state, not as an auth failure.

- Conflict risks:
  - Frontend code that derives percent from local assumptions can drift from backend truth.
  - Frontend code that increments attempts on every resume click will diverge from backend behavior.
  - Frontend code that assumes completed courses can never reopen will miss the case where new lessons are later added and step sync reintroduces incomplete work.

### 4.11 `GET /api/admin/courses`

- Auth requirement: Admin only
- Purpose: List courses for admin CRUD
- Request body: none
- Success response:
  - `200 OK`
  - Body is a raw JSON array of `AdminCourseResponse`
- Important statuses:
  - `200`
  - `401` when unauthenticated
  - `403` when authenticated without admin role
- Frontend handling notes:
  - This payload does not include lessons. It includes only `lessonCount`.
- Conflict risks:
  - Do not assume this endpoint accepts or returns public course-slug URLs for CRUD operations. Course updates and deletes still require numeric `courseId`.

### 4.12 `GET /api/admin/courses/{courseId}`

- Auth requirement: Admin only
- Purpose: Load a single course for admin editing
- Path parameter:
  - `courseId`: numeric course ID
- Success response:
  - `200 OK`
  - Body is `AdminCourseResponse`
- Important statuses:
  - `200`
  - `401`
  - `403`
  - `404` when the course ID does not exist
- Frontend handling notes:
  - Admin course edit forms should be keyed by `id`, not by slug.
- Conflict risks:
  - Frontend code that uses a slug in this route will fail.

### 4.13 `POST /api/admin/courses`

- Auth requirement: Admin only
- Purpose: Create a course
- Request JSON:

| Field | Type | Required | Constraints | Notes |
| --- | --- | --- | --- | --- |
| `slug` | string | yes | lowercase/hyphen regex, max `120` | Trimmed and lowercased again in service |
| `title` | string | yes | not blank, max `180` | Trimmed |
| `subtitle` | string | no | max `240` | Blank strings become `null` |
| `description` | string | yes | not blank, max `2000` | Trimmed |
| `locale` | string | yes | not blank, max `20` | Lowercased on save |
| `instructorName` | string | no | max `120` | Blank strings become `null` |
| `level` | string | no | max `50` | Blank strings become `null` |
| `durationHours` | number | yes | integer `1..1000` | Required |

- Success response:
  - `201 Created`
  - Body is `AdminCourseResponse`
- Important statuses:
  - `201`
  - `400` on validation failure
  - `401`
  - `403`
  - `409` when slug is already taken
- Frontend handling notes:
  - After create, replace optimistic client state with the returned object because optional blank strings may come back as `null` and locale/slug are normalized.
- Conflict risks:
  - Do not assume the backend preserves empty strings. It converts optional blank strings to `null`.

### 4.14 `PUT /api/admin/courses/{courseId}`

- Auth requirement: Admin only
- Purpose: Update an existing course
- Path parameter:
  - `courseId`: numeric course ID
- Request JSON:
  - Same shape and validation as admin course create
- Success response:
  - `200 OK`
  - Body is `AdminCourseResponse`
- Important statuses:
  - `200`
  - `400`
  - `401`
  - `403`
  - `404` when course ID is missing
  - `409` when slug conflicts with another course
- Frontend handling notes:
  - Use server response as canonical state after save.
- Conflict risks:
  - Frontend code that edits by slug instead of by ID will not match the actual route contract.

### 4.15 `DELETE /api/admin/courses/{courseId}`

- Auth requirement: Admin only
- Purpose: Delete a course if it is safe to delete
- Path parameter:
  - `courseId`: numeric course ID
- Request body: none
- Success response:
  - `204 No Content`
- Important statuses:
  - `204` on successful delete
  - `401`
  - `403`
  - `404` when course ID is missing
  - `409` with message `Cannot delete a course that already has enrollments`
  - `409` with message `Database constraint violation` is also possible if a lower-level database constraint fires
- Frontend handling notes:
  - Do not parse JSON on successful delete.
  - Show specific conflict messaging when the course cannot be deleted.
- Conflict risks:
  - Frontend code that treats all `409` responses as the same generic failure will hide meaningful conflict reasons.
  - Inferred risk: a pending `lesson_video_uploads` row can cause a lower-level data-integrity `409`, because explicit delete safety only checks enrollments.

### 4.16 `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload`

- Auth requirement: Admin only
- Purpose: Create a pending upload record and return storage-upload instructions
- Path parameters:
  - `courseSlug`: exact course slug
  - `lessonSlug`: exact lesson slug
- Request JSON:

| Field | Type | Required | Constraints | Notes |
| --- | --- | --- | --- | --- |
| `fileName` | string | yes | not blank | No explicit max-length validation in DTO |
| `contentType` | string | yes | not blank | Must also be in allowed content types at runtime |
| `sizeBytes` | number | yes | `>= 1` | Must also be `<= app.media.video.max-file-size-bytes` |

- Success response:
  - `200 OK`
  - Body is `AdminVideoUploadInitResponse`
- Important statuses:
  - `200`
  - `400` for validation, unsupported content type, or oversize file
  - `401`
  - `403`
  - `404` when course or lesson slug does not resolve
- Confirmed behavior:
  - Any previous pending upload for the lesson is deleted before a new one is created.
  - Returned `objectKey` is generated by the backend and includes a UUID plus a sanitized lowercase filename.
  - Returned `requiredHeaders` currently includes `Content-Type`.
- Frontend handling notes:
  - Use the returned `objectKey`, `uploadUrl`, and `requiredHeaders` exactly as returned.
  - Do not derive the object key client-side.
  - Treat `playbackUrl` as provisional until upload-complete succeeds.
- Conflict risks:
  - Local default profile returns `uploadUrl` with `inmemory://...`, not a browser-usable HTTP upload URL.
  - Inferred risk: very long filenames are not DTO-validated against DB column lengths and can surface as lower-level persistence errors.

### 4.17 `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete`

- Auth requirement: Admin only
- Purpose: Validate the uploaded object, attach video metadata to the lesson, and clear the pending upload row
- Path parameters:
  - `courseSlug`: exact course slug
  - `lessonSlug`: exact lesson slug
- Request JSON:

| Field | Type | Required | Constraints | Notes |
| --- | --- | --- | --- | --- |
| `objectKey` | string | yes | not blank | Must match the current pending upload row for the lesson |

- Success response:
  - `204 No Content`
- Important statuses:
  - `204`
  - `400` when upload session expired, uploaded content type mismatches, or uploaded size mismatches
  - `401`
  - `403`
  - `404` when the lesson is missing, the pending upload row is missing, or the uploaded object cannot be found in storage
- Confirmed behavior:
  - On success, the lesson's `videoUrl` is set from `VideoStorageService.getPlaybackUrl(objectKey)`.
  - The previous stored object is deleted best-effort if the object key changed.
  - The pending upload row is deleted on success.
- Frontend handling notes:
  - Do not parse JSON on success.
  - Only call this after the direct upload has completed successfully.
  - If it fails due to expiration or missing object, restart from upload-init.
- Conflict risks:
  - Completing an old object key after starting a new upload will fail, because only one pending upload per lesson is kept.

### 4.18 `DELETE /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video`

- Auth requirement: Admin only
- Purpose: Remove the lesson's stored video metadata and delete any pending upload row for that lesson
- Path parameters:
  - `courseSlug`: exact course slug
  - `lessonSlug`: exact lesson slug
- Request body: none
- Success response:
  - `204 No Content`
- Important statuses:
  - `204`
  - `401`
  - `403`
  - `404` when course or lesson slug does not resolve
- Confirmed behavior:
  - Clears `videoUrl`, storage metadata, and pending upload rows.
  - Storage object deletion is best-effort and logged on failure, but API still returns `204` on the happy path.
- Frontend handling notes:
  - Refetch the lesson payload after delete if the UI needs the new canonical `videoUrl = null` state.
- Conflict risks:
  - Do not parse JSON on success.

### 4.19 Admin moderation endpoints

#### `GET /api/admin/courses/pending`

- Auth requirement: Admin only
- Purpose: Return the moderation queue — courses in `PENDING_REVIEW` status
- Success response:
  - `200 OK`
  - Body is a raw JSON array of `AdminCourseResponse`
- Important statuses:
  - `200`, `401`, `403`

#### `POST /api/admin/courses/{courseId}/publish`

- Auth requirement: Admin only
- Purpose: Approve a course in `PENDING_REVIEW` and transition it to `PUBLISHED`
- Path parameter: `courseId` numeric
- Request body: none
- Success response:
  - `200 OK`
  - Body is `AdminCourseResponse`
- Important statuses:
  - `200` on success
  - `400` when the course is not in `PENDING_REVIEW`
  - `401`, `403`, `404`

#### `POST /api/admin/courses/{courseId}/reject`

- Auth requirement: Admin only
- Purpose: Reject a course in `PENDING_REVIEW` and transition it to `REJECTED`
- Path parameter: `courseId` numeric
- Request JSON:

| Field | Type | Required | Constraints | Notes |
| --- | --- | --- | --- | --- |
| `reason` | string | yes | length `10..1000` | Shown to the teacher |

- Success response:
  - `200 OK`
  - Body is `AdminCourseResponse`
- Important statuses:
  - `200` on success
  - `400` when the course is not in `PENDING_REVIEW`, or `reason` fails validation
  - `401`, `403`, `404`

### 4.20 Teacher course endpoints under `/api/teacher/courses`

All teacher endpoints require `TEACHER` role. A teacher can only read and mutate courses they own. Accessing another teacher's course returns `403`.

#### `GET /api/teacher/courses`

Returns a raw JSON array of `TeacherCourseResponse` for all courses owned by the calling teacher, regardless of status.

#### `GET /api/teacher/courses/{courseSlug}`

Returns `TeacherCourseResponse` for one owned course. Returns `403` if the teacher does not own it.

#### `POST /api/teacher/courses`

- Purpose: Create a new course in `DRAFT` status
- Request JSON (`TeacherCourseRequest`):

| Field | Type | Required | Constraints | Notes |
| --- | --- | --- | --- | --- |
| `slug` | string | no | lowercase/hyphen, max `120` | Auto-generated from `title` if omitted |
| `title` | string | yes | not blank, max `180` | |
| `subtitle` | string | no | max `240` | |
| `description` | string | yes | not blank, max `2000` | |
| `locale` | string | yes | not blank, max `20` | |
| `level` | string | no | max `50` | |
| `durationHours` | number | yes | integer `1..1000` | |
| `price` | number | no | `>= 0.00`, max 8 integer digits + 2 decimal | `null` or absent means free |

- Success response: `201 Created` with `TeacherCourseResponse`
- Important statuses: `201`, `400`, `401`, `403`, `409` (slug conflict)

#### `PUT /api/teacher/courses/{courseSlug}`

- Purpose: Update course metadata
- Allowed only when status is `DRAFT` or `REJECTED`
- Same request shape as `POST /api/teacher/courses`
- Returns `200 OK` with `TeacherCourseResponse`
- Returns `400` if status is not editable

#### `DELETE /api/teacher/courses/{courseSlug}`

- Purpose: Delete a `DRAFT` course with no enrollments
- Returns `204 No Content`
- Returns `409` if enrollments exist or status is not `DRAFT`

#### `POST /api/teacher/courses/{courseSlug}/submit`

- Purpose: Submit for admin review: `DRAFT` or `REJECTED` → `PENDING_REVIEW`
- Returns `200 OK` with `TeacherCourseResponse`

#### `POST /api/teacher/courses/{courseSlug}/publish`

- Purpose: Publish directly: `DRAFT` or `REJECTED` → `PUBLISHED`
- Returns `200 OK` with `TeacherCourseResponse`

#### `POST /api/teacher/courses/{courseSlug}/withdraw`

- Purpose: Withdraw from review before admin acts: `PENDING_REVIEW` → `DRAFT`
- Returns `200 OK` with `TeacherCourseResponse`

### 4.21 Teacher lesson endpoints under `/api/teacher/courses/{courseSlug}/lessons`

All require `TEACHER` role and course ownership. Lessons can be managed when the course is in `DRAFT` or `REJECTED` status.

#### `GET /api/teacher/courses/{courseSlug}/lessons`

Returns a raw JSON array of `TeacherLessonResponse`.

#### `POST /api/teacher/courses/{courseSlug}/lessons`

- Purpose: Create a lesson in the course
- Request JSON (`TeacherLessonRequest`):

| Field | Type | Required | Constraints | Notes |
| --- | --- | --- | --- | --- |
| `slug` | string | no | lowercase/hyphen, max `120` | Auto-generated from `title` if omitted |
| `title` | string | yes | not blank, max `180` | |
| `summary` | string | no | max `500` | |
| `content` | string | yes | not blank, max `8000` | |
| `videoUrl` | string | no | max `400` | For external video links; platform uploads use the video-upload workflow |
| `durationMinutes` | number | no | `1..600` | |

- Success response: `201 Created` with `TeacherLessonResponse`

#### `PUT /api/teacher/courses/{courseSlug}/lessons/{lessonSlug}`

Same request shape as create. Returns `200 OK` with `TeacherLessonResponse`.

#### `DELETE /api/teacher/courses/{courseSlug}/lessons/{lessonSlug}`

Returns `204 No Content`.

#### Teacher lesson video upload

Same workflow as admin video upload (see sections 4.16–4.18), but under the teacher path prefix:
- `POST /api/teacher/courses/{courseSlug}/lessons/{lessonSlug}/video-upload`
- `POST /api/teacher/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete`
- `DELETE /api/teacher/courses/{courseSlug}/lessons/{lessonSlug}/video`

Ownership is validated before delegating to the shared video service.

## 5. Frontend-Critical Status and Error Semantics

### 5.1 Confirmed status handling

| Status | Backend meaning | Body presence | Frontend handling requirement |
| --- | --- | --- | --- |
| `200` | Successful read or login/upload-init/update | JSON body present | Parse JSON normally |
| `201` | Successful creation | JSON body present | Parse JSON and use returned canonical object |
| `204` | Successful delete or upload-complete | No body | Do not parse JSON |
| `400` | Validation error or domain bad-request | JSON `ApiErrorResponse` | Surface message and field errors |
| `401` | Missing auth, invalid token, deleted-user token, or invalid login | JSON `ApiErrorResponse` | Clear auth state on protected-route `401`; show login failure on login-route `401` |
| `403` | Authenticated but forbidden | JSON `ApiErrorResponse` | Show access denied and prevent hidden retries |
| `404` | Missing course, lesson, upload, or other resource | JSON `ApiErrorResponse` | Render not-found state |
| `409` | Duplicate or conflict | JSON `ApiErrorResponse` | Show conflict-specific guidance |
| `429` | Rate limit exceeded | JSON `ApiErrorResponse` | Back off and tell user to retry later |

### 5.2 Error-body format

All application-level error responses use `ApiErrorResponse`.

Security-generated `401` and `403` also use `ApiErrorResponse`.

Confirmed examples:
- Unauthenticated protected route: `401`, message `Authentication required`
- Invalid login: `401`, message `Invalid email or password`
- Learner querying another learner's enrollments: `403`, message `Students can only view their own enrollments`
- Non-admin hitting admin route: `403`, message `Access denied`
- Duplicate enrollment: `409`, message `Student already enrolled in course: {courseSlug}`
- Course delete with enrollments: `409`, message `Cannot delete a course that already has enrollments`
- Rate limit: `429`, message `Rate limit exceeded. Please try again later.`
- Validation failure: `400`, message `Validation failed`, plus populated `validationErrors`

### 5.3 Response-body absence

Confirmed no-body backend responses:
- `DELETE /api/admin/courses/{courseId}` -> `204`
- `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete` -> `204`
- `DELETE /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video` -> `204`

Frontend must not call `response.json()` on these successful `204` responses.

### 5.4 Time-format warning

Confirmed current serialization behavior:
- `EnrollmentResponse.enrolledAt` is a serialized `LocalDateTime`
- `ApiErrorResponse.timestamp` is a serialized `LocalDateTime`
- `AdminVideoUploadInitResponse.expiresAt` is a serialized `Instant`

Practical consequence:
- `enrolledAt` and `timestamp` do not carry an explicit timezone offset in the JSON payload.
- The backend creates them in UTC, but the serialized payload does not include `Z`.
- Frontend code must not assume these strings are offset-aware ISO timestamps.

## 6. Auth and Session Contract

### 6.1 Login and registration behavior

Confirmed behavior:
- Registration returns an access token immediately.
- Login returns an access token immediately.
- Both return the embedded `user` object.
- There is no refresh token.
- There is no logout endpoint.
- There is no cookie-based session.

### 6.2 JWT expectations

Confirmed behavior:
- Token must be sent as `Authorization: Bearer <token>`.
- JWT expiration comes from `app.security.jwt.expiration`, default `24h`.
- JWT payload contains subject/email, role, and fullName claims.
- The backend does not trust those claims alone for authenticated requests.
- The JWT filter loads the current user from the database on each request.

Practical frontend rule:
- Client-decoded token claims are not the source of truth for long-lived session state.
- `/api/auth/me` and protected-route responses are more authoritative.

### 6.3 `/api/auth/me` usage

Recommended use:
- On app startup, if a token exists, call `/api/auth/me`.
- If `/api/auth/me` returns `200`, hydrate authenticated state from that response.
- If `/api/auth/me` returns `401`, clear the token and authenticated state.

### 6.4 Malformed, expired, and deleted-user token behavior

Confirmed behavior:
- Missing token on protected route -> `401 Authentication required`
- Malformed token on protected route -> `401 Authentication required`
- Expired token on protected route -> `401 Authentication required`
- Deleted-user token on protected route -> `401 Authentication required`

Important nuance:
- Invalid tokens are ignored on public routes. Public routes still work because the filter clears auth context and continues.
- Invalid tokens do not produce special "token malformed" message strings.

### 6.5 What frontend must do on `401`

- Clear stored token
- Clear user/session state
- Stop retry loops
- Redirect to login or present a re-authentication flow
- Preserve user-entered form state only where appropriate

### 6.6 What frontend must not do

- Do not keep using a token after `/api/auth/me` or another protected route returns `401`.
- Do not decide whether a user exists or must register based on the login error message.
- Do not rely on a decoded token as the only source of truth for user role.
- Do not implement eLearning auth against legacy `json-server`.

## 7. Enrollment Contract

### 7.1 Anonymous enrollment behavior

Confirmed behavior:
- `POST /api/enrollments` is public.
- A completely unauthenticated user can enroll.
- If the email does not exist, the backend creates a `platform_users` row with no password hash.
- That row acts as a lead-shell user record.

### 7.2 Lead-shell behavior

Confirmed behavior:
- A passwordless lead-shell user can later register through `/api/auth/register` with the same email.
- Registration upgrades the existing user row instead of creating a duplicate user.
- Existing enrollments remain attached because the same user record is reused.

### 7.3 Non-mutation rules

Confirmed behavior:
- A repeated enrollment for the same course returns `409` and does not mutate the existing lead profile.
- A new enrollment for a different course reuses the same passwordless lead-shell user.
- When reusing that passwordless lead-shell user, `fullName` and `locale` are updated only if the existing values are blank.
- Nonblank lead-shell values are preserved.

### 7.4 Duplicate enrollment behavior

Confirmed behavior:
- Duplicate course enrollment for the same user returns `409 Conflict`
- Message: `Student already enrolled in course: {courseSlug}`

Frontend implication:
- This is not a generic failure.
- This is a valid domain state that should be surfaced intentionally.

### 7.5 Rate-limit behavior

Confirmed behavior:
- Enrollment POST is rate-limited by client IP or `X-Forwarded-For`.
- Default limit is `10` requests per `1` minute window from shared config.
- The filter increments usage before controller execution.

Practical consequence:
- Successful enrollments, failed validations, and conflict responses all still consume rate-limit budget.

### 7.6 What frontend should surface to users

- `201`: enrollment success confirmation
- `409`: "already enrolled" or equivalent duplicate state
- `404`: course no longer exists or slug is invalid
- `429`: retry-later message, not a silent failure
- `400`: form-level validation problems

### 7.7 Progress bootstrap on enrollment

Confirmed behavior:
- Successful enrollment initializes a `course_progress` aggregate for the learner and course.
- The initialized aggregate starts in `NOT_STARTED`.
- `completedSteps = 0`, `percentComplete = 0`, and `attemptCount = 0`.
- One `course_progress_steps` row is created for each current lesson in the course.
- If the learner later registers from a lead-shell account, that same user record keeps the existing progress state.

Frontend implication:
- Enrollment does not return progress inline.
- Progress data becomes available through authenticated progress endpoints after the learner has a valid JWT.

### 7.8 Progress lifecycle

Confirmed behavior:
- `POST /api/progress/courses/{courseSlug}/start` starts a new attempt only from `NOT_STARTED` or `RESET`.
- Completing a step recalculates counters and percent on the backend.
- Completing all steps changes status to `COMPLETED` and clears `currentStep`.
- Explicit full completion supports zero-step courses and returns `percentComplete = 100`.
- `POST /api/progress/courses/{courseSlug}/reset` clears completed steps and sets status to `RESET`.
- Step rows stay synchronized with the course lesson list over time.

Frontend implication:
- Do not treat public lesson reads as implicit progress writes.
- Do not keep a separate client-owned notion of progress percent or step order.

### 7.9 What frontend should surface for progress

- `200`: render the returned progress snapshot as canonical state
- `400`: learner is authenticated but not enrolled in the requested course
- `401`: learner needs a valid authenticated session before reading or mutating progress
- `404`: course or lesson slug is invalid for the requested progress route
- `RESET`: explicit restart state, not an error
- `COMPLETED`: final state, except when later lesson-sync changes reopen the course with new incomplete steps

## 8. Admin Contract

### 8.1 Public vs authenticated vs admin-only route map

| Visibility | Routes |
| --- | --- |
| Public | `GET /api/health`, `POST /api/auth/register`, `GET /api/auth/verify`, `POST /api/auth/login`, `GET /api/courses`, `GET /api/courses/{slug}`, `GET /api/courses/{courseSlug}/lessons/{lessonSlug}`, `POST /api/enrollments` |
| Authenticated | `GET /api/auth/me`, `GET /api/enrollments`, `POST /api/courses/{slug}/ratings`, `GET /api/progress/courses/{courseSlug}`, `POST /api/progress/courses/{courseSlug}/start`, `PUT /api/progress/courses/{courseSlug}/current-step`, `POST /api/progress/courses/{courseSlug}/steps/{lessonSlug}/complete`, `POST /api/progress/courses/{courseSlug}/complete`, `POST /api/progress/courses/{courseSlug}/reset` |
| Teacher only | `/api/teacher/**` |
| Admin only | `/api/admin/**` |

### 8.2 Admin-only routes

Confirmed admin-only routes:
- `GET /api/admin/courses`
- `GET /api/admin/courses/pending`
- `GET /api/admin/courses/{courseId}`
- `POST /api/admin/courses`
- `PUT /api/admin/courses/{courseId}`
- `DELETE /api/admin/courses/{courseId}`
- `POST /api/admin/courses/{courseId}/publish`
- `POST /api/admin/courses/{courseId}/reject`
- `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload`
- `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete`
- `DELETE /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video`

### 8.3 Regular-user rejection behavior

Confirmed behavior:
- No token on admin route -> `401 Authentication required`
- Student or teacher token on admin route -> `403 Access denied`
- No token on teacher route -> `401 Authentication required`
- Student or admin token on teacher route -> `403 Access denied`

### 8.4 Frontend route-guarding recommendations

- Guard admin UI routes using authenticated `user.role === 'ADMIN'`.
- Guard teacher UI routes using authenticated `user.role === 'TEACHER'`.
- Still handle server-side `401` and `403`, because stale client state can drift.
- Keep learner, teacher, and admin routes separate in router ownership and layout composition.

### 8.5 Important admin-flow limitations

Confirmed limitations:
- There is no backend endpoint in this repo to create admin users.
- There is no admin lesson CRUD API. Lesson CRUD belongs to the teacher role.
- Admin course list endpoints return `lessonCount` but not lesson data.

Frontend implication:
- If admin UI needs lesson outlines, use the public course landing endpoint or the teacher lesson endpoints.
- Do not invent lesson-admin endpoints in the frontend integration layer.

### 8.6 Course status lifecycle

Courses follow a moderation workflow:

| Status | Who sets it | Editable by teacher | Visible in public catalog |
| --- | --- | --- | --- |
| `DRAFT` | Created by teacher | Yes | No |
| `PENDING_REVIEW` | Teacher submits | No (locked) | No |
| `PUBLISHED` | Admin approves | No | Yes |
| `REJECTED` | Admin rejects | Yes (can resubmit) | No |

Frontend implications:
- Only show the "Edit" and "Add Lesson" actions when `status` is `DRAFT` or `REJECTED`.
- Show `rejectionReason` from `TeacherCourseResponse` when `status == REJECTED`.
- Show the "Withdraw" action only when `status == PENDING_REVIEW`.
- The public course catalog only contains `PUBLISHED` courses.

### 8.6 Local admin video caveat

Confirmed behavior:
- The default local profile uses the in-memory video provider.
- In-memory upload-init returns `uploadUrl` values like `inmemory://bucket/key`.
- There is no backend HTTP route in this repo that accepts those upload URLs.
- There is also no backend route serving `http://localhost:7777/media/videos/**`.

Frontend implication:
- Default local profile does not provide a browser-usable end-to-end admin upload transport.
- For real browser upload testing, use an environment with the S3 provider configured.

## 9. Data and Schema Notes

### 9.1 Canonical schema source

Canonical schema source:
- `src/main/resources/db/migration/V1__init_schema.sql`
- `src/main/resources/db/migration/V2__add_lesson_video_uploads.sql`
- `src/main/resources/db/migration/V3__add_course_progress_tracking.sql`
- `src/main/resources/db/migration/V4__add_teacher_role_and_course_status.sql`
- `src/main/resources/db/migration/V5__add_email_verification.sql`
- `src/main/resources/db/migration/V6__add_course_price_and_ratings.sql`

`docs/mvp-schema.sql` is not the canonical schema anymore.

### 9.2 Frontend-relevant tables and constraints

| Table | Important columns | Important constraints |
| --- | --- | --- |
| `courses` | `slug`, `title`, `subtitle`, `description`, `locale`, `instructor_name`, `level`, `duration_hours`, `price`, `status`, `rejection_reason`, `owner_id` | `uk_courses_slug` |
| `lessons` | `course_id`, `slug`, `title`, `summary`, `content`, `video_url`, `position`, `duration_minutes`, video metadata fields | `uk_lessons_slug_per_course` |
| `platform_users` | `full_name`, `email`, `locale`, `role`, `password_hash`, `email_verified`, `verification_token`, `verification_token_expires_at` | `uk_platform_users_email` |
| `enrollments` | `course_id`, `student_id`, `status`, `enrolled_at` | `uk_enrollments_course_user` |
| `lesson_video_uploads` | `lesson_id`, `object_key`, `original_filename`, `content_type`, `size_bytes`, `expires_at` | one pending upload per lesson, unique object key |
| `course_progress` | `course_id`, `student_id`, `current_lesson_id`, `status`, `total_steps`, `completed_steps`, `percent_complete`, `attempt_count`, `started_at`, `completed_at`, `reset_at` | one aggregate progress row per `(course, student)` |
| `course_progress_steps` | `progress_id`, `lesson_id`, `step_order`, `status`, `completed_at` | one progress-step row per `(progress, lesson)` |
| `course_ratings` | `course_id`, `student_id`, `rating` | one rating per `(course, student)` — upsert semantics |

### 9.3 Slug behavior

Confirmed behavior:
- Course slugs are globally unique.
- Admin course DTO enforces lowercase letters, numbers, and hyphens.
- Admin create/update lowercases slugs again in the service layer.
- Public course and lesson routes expect exact slug path values.
- Lesson slugs are unique only within a course.

Frontend rule:
- Treat slugs as opaque canonical identifiers received from the backend.
- Do not generate or mutate them in client routing logic beyond normal URL encoding.

### 9.4 Field nullability the frontend must tolerate

Frontend must tolerate `null` for:
- `CourseSummaryResponse.subtitle`
- `CourseSummaryResponse.level`
- `CourseSummaryResponse.durationHours`
- `CourseSummaryResponse.instructorName`
- `CourseSummaryResponse.price`
- `CourseSummaryResponse.averageRating`
- `CourseLandingResponse.subtitle`
- `CourseLandingResponse.instructorName`
- `CourseLandingResponse.level`
- `CourseLandingResponse.durationHours`
- `LessonOutlineResponse.durationMinutes`
- `LessonOutlineResponse.summary`
- `LessonViewerResponse.durationMinutes`
- `LessonViewerResponse.videoUrl`
- `CourseProgressResponse.currentStep`
- `CourseProgressResponse.startedAt`
- `CourseProgressResponse.completedAt`
- `CourseProgressResponse.resetAt`
- `CourseProgressStepResponse.completedAt`
- `AdminCourseResponse.subtitle`
- `AdminCourseResponse.instructorName`
- `AdminCourseResponse.level`
- `TeacherCourseResponse.subtitle`
- `TeacherCourseResponse.level`
- `TeacherCourseResponse.price`
- `TeacherCourseResponse.rejectionReason`
- `TeacherLessonResponse.summary`
- `TeacherLessonResponse.durationMinutes`
- `TeacherLessonResponse.videoUrl`

### 9.5 Uniqueness and conflict assumptions

Confirmed uniqueness rules:
- One course per slug
- One platform user per email
- One lesson slug per course
- One enrollment per `(course, student)`
- One pending lesson video upload row per lesson
- One course-progress aggregate per `(course, student)`
- One course-progress step row per `(progress, lesson)`
- One rating per `(course, student)` — submitting again replaces the existing rating

Frontend implication:
- `409` is part of normal domain handling, not an exceptional crash-only condition.

### 9.6 Progress schema and synchronization rules

Confirmed behavior:
- Aggregate progress stores counters, timestamps, current step, and attempt history separately from the step rows.
- Step rows are keyed by lesson identity, not by arbitrary frontend step labels.
- Progress synchronization is lesson-driven: new lessons are added as `NOT_STARTED` steps, removed lessons are dropped, and counters are recalculated afterward.

Frontend implication:
- Client code must not invent a separate persistent step inventory.
- Backend progress can change when the course lesson set changes, even if the learner did not submit a new progress mutation at that moment.

### 9.7 Delete safety and conflict rules

Confirmed behavior:
- Course delete is blocked if enrollments exist.
- Lesson video delete clears lesson metadata and pending uploads.

Inferred risk:
- Backend does not explicitly manage `lesson_video_uploads` when deleting a course.
- A pending upload row can therefore surface as a generic `409 Database constraint violation`.
- This is inferred from the schema and entity mapping and is not directly covered by a checked-in integration test.

### 9.8 Validation asymmetries that can affect frontend forms

Confirmed asymmetries:
- Register email max length is `120`
- Enrollment email max length is `180`
- Register locale max length is `10`
- Enrollment locale max length is `20`

Frontend recommendation:
- If a single UI collects data used by both register and enrollment flows, validate to the stricter shared limits, not the looser enrollment-only limits.

## 10. Configuration and Environment Notes

### 10.1 Default profile

Confirmed shared config:
- Default Spring profile is `local`
- Server port is `7777`
- JWT expiration default is `24h`
- CORS allowed origins default to `http://localhost:3000` and `http://localhost:5173`

### 10.2 Local profile behavior

Confirmed local profile:
- H2 in-memory database
- H2 console enabled at `/h2-console`
- media provider is `in-memory`
- local JWT secret comes from `APP_SECURITY_JWT_SECRET_LOCAL` with a local default fallback

Frontend-relevant local caveats:
- The backend API is usable locally.
- Admin upload transport is not browser-usable locally by default because the upload URL is `inmemory://...`.
- The configured local playback base URL is `http://localhost:7777/media/videos`, but this repo does not define a controller or static route serving that path.

### 10.3 Non-local fail-closed behavior

Confirmed non-local behavior:
- `prod` profile groups in the `postgres` profile
- PostgreSQL datasource settings are required from env
- S3 video storage settings are required from env
- JWT secret is required from env
- Checked-in test coverage confirms startup fails fast when `APP_SECURITY_JWT_SECRET` is missing for the postgres profile path

Frontend implication:
- Environment mismatches will fail backend startup rather than silently falling back to unsafe defaults.

### 10.4 Seeder behavior

Confirmed behavior:
- `DataSeeder` runs on startup
- If `courseRepository.count() == 0`, it inserts:
  - course slug `digital-skills-kz`
  - two lessons
- This seeder is not restricted to the local profile

Frontend implication:
- Fresh empty environments will auto-seed a demo course unless the seeder is changed.
- Older frontend assumptions that "empty DB means empty catalog" are not always true.

### 10.5 CORS and auth transport

Confirmed behavior:
- CORS allows `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- Allowed headers are `*`
- No credentialed-cookie contract is configured

Frontend implication:
- Use bearer tokens in headers.
- Do not build the eLearning frontend around cookie/session assumptions.

## 11. Conflict Prevention Rules for Frontend Agent

These rules are mandatory if the goal is to minimize cross-repo conflicts.

- Do not assume frontend code lives in this repository.
- Do not treat old frontend code or old docs as the contract source of truth when backend code disagrees.
- Do not parse JSON on any successful `204` response.
- Do not treat `409` as a generic failure. Distinguish duplicate enrollment, course delete conflicts, slug conflicts, and lower-level integrity conflicts.
- Do not force registration before enrollment. Public anonymous enrollment is intentional backend behavior.
- Do not use legacy `json-server` auth for eLearning flows.
- Do not silently swallow `401`, `403`, or `429`.
- Do not infer DTO shapes from memory or old frontend models. Use backend DTOs and this contract.
- Do not expose admin actions to non-admin users, and do not expose teacher actions to non-teacher users.
- Do not assume lesson viewing requires auth. It is public in the current backend.
- Do not assume course/list responses contain pagination wrappers or enrollment state.
- Do not assume admin CRUD routes use course slug. Admin course edit/delete routes use numeric `courseId`.
- Do not assume the admin upload-init `uploadUrl` is always browser-usable HTTP(S). In local profile it is not.
- Do not assume the backend serves uploaded video files from `http://localhost:7777/media/videos/**`.
- Do not assume login errors distinguish "bad password" from "lead-shell account with no password".
- Do not make locale casing semantically meaningful. Register and enrollment flows normalize differently.
- Do not allow frontend form constraints to be looser than backend register constraints if the same data might later be reused in registration.
- Do not parse `LocalDateTime` strings as timezone-safe UTC unless the frontend deliberately applies a convention.
- Do not assume `POST /api/auth/register` always returns `AuthResponse`. It may return `MessageResponse` when email verification is required. Check the response shape before storing a token.
- Do not show teacher course edit/lesson UI when the course status is `PENDING_REVIEW` or `PUBLISHED`. Only `DRAFT` and `REJECTED` are editable by the teacher.
- Do not assume the public course catalog contains DRAFT, PENDING_REVIEW, or REJECTED courses. Only PUBLISHED courses appear there.
- Do not assume rating submission returns a body. It returns `204 No Content`.
- Do not allow a teacher to act on another teacher's course. The backend returns `403`, but the frontend should not present the action in the first place.

## 12. Recommended Frontend Implementation Pattern

### 12.1 Centralized API client

Use one centralized eLearning API client that:
- owns the backend base URL
- attaches `Authorization` for protected/admin routes
- skips JSON parsing on `204`
- parses `ApiErrorResponse`
- normalizes list endpoints that return raw arrays
- cleanly separates backend origin `7777` from legacy origin `3001`

### 12.2 Token attachment strategy

Recommended pattern:
- Store the access token once in a centralized auth store
- Attach `Authorization: Bearer <token>` to protected/admin backend requests
- It is acceptable to attach the token to public backend requests, but public behavior must not depend on it
- Do not attach the token to legacy `json-server` calls unless that separate legacy contract explicitly requires it

### 12.3 Session restoration pattern

Recommended startup flow:
1. Read stored token
2. If no token, stay anonymous
3. If token exists, call `GET /api/auth/me`
4. On `200`, hydrate session from response
5. On `401`, clear token and authenticated state

### 12.4 Environment-based API origins

Keep eLearning and legacy origins separate in frontend env configuration.

Recommended concept:
- `ELEARNING_API_ORIGIN=http://localhost:7777`
- `LEGACY_API_ORIGIN=http://localhost:3001`

Do not bury these origins inside feature components.

### 12.5 Error normalization

Normalize backend errors by status:
- `400`: inline form errors from `validationErrors`, plus top-level message
- `401`: session-expired or invalid-login handling depending route
- `403`: access denied
- `404`: not found page/state
- `409`: domain conflict messaging
- `429`: retry-later state and temporary submit disable

Important rule:
- Branch on HTTP status first.
- Do not make control-flow decisions from free-text `message` alone.

### 12.6 Route ownership separation

Keep route ownership explicit:
- legacy flows stay on legacy data contract
- eLearning flows use this backend contract
- do not mix auth/session state between the two domains without deliberate design

### 12.7 User feedback expectations

Recommended UI behavior:
- form validation -> inline errors
- enrollment duplicate -> friendly conflict toast/inline message
- auth/session loss -> redirect or modal plus session clear
- admin forbidden -> explicit access denied screen
- rate limit -> visible retry-later feedback

### 12.8 Admin video upload implementation pattern

Recommended sequence:
1. Call upload-init
2. Read `uploadUrl`, `objectKey`, `requiredHeaders`, `expiresAt`
3. If `uploadUrl` is not an HTTP(S) URL, surface an environment limitation instead of pretending upload is available
4. Upload the file directly to the returned storage URL with the required headers
5. Call upload-complete with `{ objectKey }`
6. Refetch lesson data if the UI needs canonical `videoUrl`

### 12.9 Progress integration pattern

Recommended sequence:
1. After the learner is authenticated, fetch `GET /api/progress/courses/{courseSlug}` for the active course.
2. Treat the returned `status`, `currentStep`, `percentComplete`, and `steps` as the canonical persisted state.
3. Call `POST /api/progress/courses/{courseSlug}/start` when the learner intentionally begins or resumes a tracked attempt.
4. Call `PUT /api/progress/courses/{courseSlug}/current-step` when the UI needs to persist active-lesson focus without completion.
5. Call `POST /api/progress/courses/{courseSlug}/steps/{lessonSlug}/complete` only for durable completion, not for temporary UI state.
6. Call `POST /api/progress/courses/{courseSlug}/reset` only from an explicit restart action.
7. Do not compute or overwrite `percentComplete` on the client.

## 13. Changed / Invalidated Assumptions

| Invalid assumption | Current backend truth | How old frontend code can break |
| --- | --- | --- |
| Frontend code exists in this repo | This repo is backend-only | An agent edits the wrong repo and fails to coordinate |
| Default backend profile is non-local Postgres | Default profile is `local` | Frontend dev setup can target the wrong runtime assumptions |
| eLearning auth can keep using `json-server` | eLearning auth is JWT against this backend | Login/session logic diverges from real backend behavior |
| Enrollment requires registration first | Enrollment is public and anonymous-friendly | Frontend blocks intentional lead capture |
| Existing email on register always means `409` | Passwordless lead-shell users are upgraded on register | Frontend blocks valid account completion |
| Login reveals whether an account is passwordless | Login always returns generic invalid-credentials `401` | Frontend branches on nonexistent signal |
| Lesson viewing is authenticated or enrollment-gated | Lesson viewer route is public | Frontend applies stricter gating than backend |
| Public course data includes enrollment state | Public course DTOs do not contain enrollment flags | Frontend tries to read fields that do not exist |
| No backend progress API exists | Authenticated progress routes exist under `/api/progress/courses/**` | Frontend can ignore server-owned progress and drift into client-only state |
| Percent complete can be derived purely on the client | Backend recalculates progress percent from persisted steps | Client percentages can diverge from server truth |
| Admin course routes are slug-based | Admin course get/update/delete use numeric `courseId` | Admin UI calls the wrong endpoints |
| `docs/mvp-schema.sql` is the schema truth | Flyway migrations are canonical | Frontend/admin tooling misses video schema additions |
| `401` only means "missing token" | `401` also covers malformed, expired, and deleted-user tokens | Frontend keeps invalid sessions alive |
| Successful delete responses contain JSON | Admin deletes and upload-complete return `204` no body | Frontend crashes on `response.json()` |
| Local admin upload works end to end in browser | Local upload-init returns `inmemory://...` and no media route exists | Frontend pretends upload/playback are available when they are not |
| All datetime fields are timezone-aware JSON instants | Some are `LocalDateTime` strings without offset | Frontend displays wrong times or mis-sorts |
| `POST /api/auth/register` always returns `AuthResponse` | When email verification is enabled it returns `MessageResponse` instead | Frontend tries to read `accessToken` from a response that has only `message` |
| Only `STUDENT` and `ADMIN` roles exist | Roles are `STUDENT`, `TEACHER`, `COMPANY`, `ADMIN` | Role-based UI branching misses teacher-specific flows |
| All courses are always public in the catalog | Only `PUBLISHED` courses appear in the public catalog | Frontend shows stale DRAFT/REJECTED courses or misses newly published ones |
| There is no lesson CRUD API | Teachers have full lesson CRUD under `/api/teacher/courses/{courseSlug}/lessons` | Teacher dashboard has no way to manage lessons |
| Admin has no moderation workflow | Admin has `GET /api/admin/courses/pending`, publish, and reject endpoints | Admin UI cannot surface the review queue |

## 14. Regression-Sensitive Areas

These behaviors should not change casually. If they do change, backend docs and tests must be updated in the same change.

- Public/authenticated/admin route visibility in `SecurityConfig`
- JSON `401` and `403` response shape from security entry points
- JWT filter behavior that reloads users from the database and converts deleted-user tokens into `401`
- Register-upgrade behavior for passwordless lead-shell users
- Generic login `401 Invalid email or password` behavior
- Enrollment duplicate `409` behavior
- Enrollment non-mutation rule for existing nonblank lead-shell profiles
- Progress bootstrap on enrollment
- Progress endpoint auth requirement and `400` not-enrolled behavior
- Backend-only calculation of `percentComplete`
- Attempt-count increment only when a new attempt begins from `NOT_STARTED` or `RESET`
- Step synchronization against the current course lesson set
- Zero-step completion semantics
- Direct list response shapes with no pagination wrapper
- `204` no-body semantics on admin delete and upload-complete routes
- Admin course CRUD using numeric `courseId`
- Admin video upload-init response fields: `objectKey`, `uploadUrl`, `requiredHeaders`, `expiresAt`, `playbackUrl`
- Upload-complete content-type and size verification
- Rate-limited routes and JSON `429` response body
- Default profile and non-local fail-closed config behavior
- Seeder behavior on empty databases

Inferred high-risk backend area:
- Course delete interaction with pending lesson video uploads is not explicitly guarded by service logic and is likely to surface as a lower-level `409` if hit.

## 15. Final Operational Guidance for Future AI Agents

Priority order:
1. Backend code and tests beat docs.
2. Security config beats UI assumptions.
3. DTO shapes beat remembered frontend models.
4. Active YAML/profile config beats old environment notes.
5. This document is the handoff artifact, but code still wins if the document becomes stale.

Backend-agent workflow:
1. If you change an endpoint path, auth requirement, DTO field, status code, or environment behavior, update tests and this document in the same pass.
2. Call out cross-repo impact explicitly, not as an afterthought.
3. Mark inferred risks separately from confirmed behavior.

Frontend-agent workflow:
1. Audit the actual frontend repo before editing.
2. Map each eLearning screen to one backend route in this document.
3. Keep legacy `json-server` flows separate from Spring backend eLearning flows.
4. Handle `401`, `403`, `404`, `409`, and `429` deliberately.
5. Use backend-returned canonical values rather than preserving optimistic assumptions.

Coordination rule for both agents:
- Prefer explicit integration notes over local optimization.
- If a change can break the other repo, document it before or alongside the code change.
- If an assumption is not verified in code, label it as unverified or inferred.
