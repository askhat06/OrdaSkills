# Frontend Integration Contract: Backend Source of Truth

Audit date: `2026-03-22`

Repository role: Spring Boot backend only

Verification status:
- Confirmed from backend code, DTOs, controllers, security config, YAML profiles, Flyway migrations, and checked-in tests.
- Checked-in tests were inspected as source material.
- Tests were not re-run in this workspace because `java` is unavailable and `JAVA_HOME` is not configured here.

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
| `src/main/java/kz/skills/elearning/controller/AdminCourseController.java` | Admin course CRUD | Admin route contract and `courseId` usage |
| `src/main/java/kz/skills/elearning/controller/AdminLessonVideoController.java` | Admin video routes | Upload-init, upload-complete, delete-video contract |
| `src/main/java/kz/skills/elearning/dto/*.java` | HTTP payload shapes | Exact request and response fields |
| `src/main/java/kz/skills/elearning/service/AuthService.java` | Register/login/current-user logic | Email normalization, lead upgrade, generic login errors |
| `src/main/java/kz/skills/elearning/service/EnrollmentService.java` | Enrollment rules | Anonymous enrollment, lead-shell behavior, admin vs learner enrollment listing |
| `src/main/java/kz/skills/elearning/service/AdminCourseService.java` | Admin CRUD rules | Slug normalization, delete conflict behavior |
| `src/main/java/kz/skills/elearning/service/AdminLessonVideoService.java` | Video workflow rules | Single pending upload, size/content-type verification, completion semantics |
| `src/main/java/kz/skills/elearning/config/SecurityConfig.java` | Route authorization and JSON `401`/`403` behavior | Public vs authenticated vs admin-only truth |
| `src/main/java/kz/skills/elearning/security/JwtAuthenticationFilter.java` | JWT parsing and DB-backed principal loading | Malformed/deleted-user token behavior |
| `src/main/java/kz/skills/elearning/security/RequestRateLimitFilter.java` | Public POST rate limiting | `429` contract |
| `src/main/resources/application.yml` | Shared defaults | Port, default profile, CORS, JWT expiry, rate-limit defaults |
| `src/main/resources/application-local.yml` | Local profile behavior | H2, H2 console, in-memory media, local JWT secret |
| `src/main/resources/application-postgres.yml` | Non-local storage/runtime behavior | PostgreSQL, S3, required env variables |
| `src/main/resources/application-prod.yml` | Production overlay | Disables H2 console |
| `src/main/resources/db/migration/V1__init_schema.sql` | Canonical base schema | Tables and uniqueness constraints |
| `src/main/resources/db/migration/V2__add_lesson_video_uploads.sql` | Video schema extension | Lesson video metadata and pending upload table |
| `src/test/java/kz/skills/elearning/ApiIntegrationTests.java` | Integration contract tests | Verifies route behavior and edge cases |
| `src/test/java/kz/skills/elearning/RateLimitingIntegrationTests.java` | Rate-limit tests | Verifies `429` behavior |
| `src/test/java/kz/skills/elearning/ProfileConfigurationTests.java` | Fail-closed config test | Verifies non-local JWT secret requirement |

### 2.2 Important stale or secondary files

| File | Status | Why it is not final truth |
| --- | --- | --- |
| `docs/PROJECT_BLUEPRINT.md` | Helpful secondary context | Contains useful guidance, but code still wins |
| `docs/REQUIREMENTS_MATRIX.md` | Helpful secondary context | Requirement tracking, not endpoint contract authority |
| `docs/mvp-schema.sql` | Stale for schema contract | Does not include lesson video upload schema added in `V2__add_lesson_video_uploads.sql` |

## 3. Implemented Backend Capabilities

### 3.1 Confirmed implemented features

- Auth registration via `POST /api/auth/register`
- Auth login via `POST /api/auth/login`
- Session restoration/current-user lookup via `GET /api/auth/me`
- Public course catalog via `GET /api/courses`
- Public course landing via `GET /api/courses/{slug}`
- Public lesson viewer via `GET /api/courses/{courseSlug}/lessons/{lessonSlug}`
- Public enrollment creation via `POST /api/enrollments`
- Authenticated enrollment listing via `GET /api/enrollments`
- Admin course list/get/create/update/delete via `/api/admin/courses`
- Admin lesson video upload-init, upload-complete, and delete via `/api/admin/courses/{courseSlug}/lessons/{lessonSlug}`
- Health check via `GET /api/health`
- Rate limiting for `POST /api/auth/login`, `POST /api/auth/register`, and `POST /api/enrollments`
- Role-based access control with `ROLE_ADMIN`
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
- Admin course deletion returns explicit `409` if enrollments exist, instead of deleting related learner data implicitly.
- Admin video upload keeps only one pending upload per lesson. Starting a new upload invalidates the previous pending upload row.

### 3.3 Confirmed absences and non-features

These are important because frontend code must not assume they exist.

- No refresh-token endpoint
- No logout endpoint
- No cookie/session auth flow
- No progress tracking API
- No assessments API
- No localized content API beyond plain `locale` strings on records
- No lesson CRUD API
- No admin lesson list endpoint
- No pagination or total-count wrapper on list endpoints
- No backend media proxy route for serving uploaded video files
- No backend endpoint that publishes allowed upload content types or max file size to the frontend at runtime
- No admin self-service account creation endpoint

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
| `role` | string | `STUDENT` or `ADMIN` |

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

- Success response:
  - `201 Created`
  - Body is `AuthResponse`
- Important statuses:
  - `201` on successful registration or successful lead-shell upgrade
  - `400` on validation failure
  - `409` if the email already belongs to a user with a nonblank password hash
  - `429` if registration rate limit is exceeded
- Frontend handling notes:
  - Treat the response `user` object as canonical. It contains normalized values.
  - Store `accessToken`, use `tokenType` as `"Bearer"`, and treat `expiresInSeconds` as the configured JWT lifetime hint.
  - If registration succeeds for an email that already enrolled anonymously, that is expected backend behavior.
- Conflict risks if frontend assumes the wrong contract:
  - If the frontend assumes every existing email returns `409`, it will block valid lead-shell upgrades.
  - If the frontend omits `locale`, the backend will silently default it to `"ru"`.
  - If the frontend allows register-email lengths above `120`, it may create an enrollment that can never be upgraded through this route.

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
- Purpose: Return the public course catalog
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
- Conflict risks:
  - Frontend code expecting `items`, `data`, or `pagination` wrappers will break.
  - Frontend code expecting the list to be alphabetical or stable by ID will not match backend ordering.

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
- Frontend handling notes:
  - Do not force registration before allowing enrollment.
  - Use `409` to show "already enrolled" messaging rather than a generic failure.
  - Use the response as confirmation of the enrollment record that was actually created.
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
- Conflict risks:
  - Frontend code that lets learners query arbitrary emails will produce backend `403`.
  - Frontend code that expects admin-only access here is too strict; regular learners can call it for themselves.

### 4.10 `GET /api/admin/courses`

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

### 4.11 `GET /api/admin/courses/{courseId}`

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

### 4.12 `POST /api/admin/courses`

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

### 4.13 `PUT /api/admin/courses/{courseId}`

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

### 4.14 `DELETE /api/admin/courses/{courseId}`

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

### 4.15 `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload`

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

### 4.16 `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete`

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

### 4.17 `DELETE /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video`

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

## 8. Admin Contract

### 8.1 Public vs authenticated vs admin-only route map

| Visibility | Routes |
| --- | --- |
| Public | `GET /api/health`, `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/courses`, `GET /api/courses/{slug}`, `GET /api/courses/{courseSlug}/lessons/{lessonSlug}`, `POST /api/enrollments` |
| Authenticated | `GET /api/auth/me`, `GET /api/enrollments` |
| Admin only | `/api/admin/**` |

### 8.2 Admin-only routes

Confirmed admin-only routes:
- `GET /api/admin/courses`
- `GET /api/admin/courses/{courseId}`
- `POST /api/admin/courses`
- `PUT /api/admin/courses/{courseId}`
- `DELETE /api/admin/courses/{courseId}`
- `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload`
- `POST /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete`
- `DELETE /api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video`

### 8.3 Regular-user rejection behavior

Confirmed behavior:
- No token on admin route -> `401 Authentication required`
- Student token on admin route -> `403 Access denied`

### 8.4 Frontend route-guarding recommendations

- Guard admin UI routes using authenticated `user.role === 'ADMIN'`.
- Still handle server-side `401` and `403`, because stale client state can drift.
- Keep learner routes and admin routes separate in router ownership and layout composition.

### 8.5 Important admin-flow limitations

Confirmed limitations:
- There is no backend endpoint in this repo to create admin users.
- There is no admin lesson CRUD API.
- There is no admin lesson list API.
- Admin course endpoints return `lessonCount` but not lesson data.

Frontend implication:
- If admin UI needs lesson outlines today, it must use the public course landing endpoint or request a backend enhancement.
- Do not invent lesson-admin endpoints in the frontend integration layer.

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

`docs/mvp-schema.sql` is not the canonical schema anymore.

### 9.2 Frontend-relevant tables and constraints

| Table | Important columns | Important constraints |
| --- | --- | --- |
| `courses` | `slug`, `title`, `subtitle`, `description`, `locale`, `instructor_name`, `level`, `duration_hours` | `uk_courses_slug` |
| `lessons` | `course_id`, `slug`, `title`, `summary`, `content`, `video_url`, `position`, `duration_minutes`, video metadata fields | `uk_lessons_slug_per_course` |
| `platform_users` | `full_name`, `email`, `locale`, `role`, `password_hash` | `uk_platform_users_email` |
| `enrollments` | `course_id`, `student_id`, `status`, `enrolled_at` | `uk_enrollments_course_user` |
| `lesson_video_uploads` | `lesson_id`, `object_key`, `original_filename`, `content_type`, `size_bytes`, `expires_at` | one pending upload per lesson, unique object key |

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
- `CourseLandingResponse.subtitle`
- `CourseLandingResponse.instructorName`
- `CourseLandingResponse.level`
- `CourseLandingResponse.durationHours`
- `LessonOutlineResponse.durationMinutes`
- `LessonOutlineResponse.summary`
- `LessonViewerResponse.durationMinutes`
- `LessonViewerResponse.videoUrl`
- `AdminCourseResponse.subtitle`
- `AdminCourseResponse.instructorName`
- `AdminCourseResponse.level`

### 9.5 Uniqueness and conflict assumptions

Confirmed uniqueness rules:
- One course per slug
- One platform user per email
- One lesson slug per course
- One enrollment per `(course, student)`
- One pending lesson video upload row per lesson

Frontend implication:
- `409` is part of normal domain handling, not an exceptional crash-only condition.

### 9.6 Delete safety and conflict rules

Confirmed behavior:
- Course delete is blocked if enrollments exist.
- Lesson video delete clears lesson metadata and pending uploads.

Inferred risk:
- Backend does not explicitly manage `lesson_video_uploads` when deleting a course.
- A pending upload row can therefore surface as a generic `409 Database constraint violation`.
- This is inferred from the schema and entity mapping and is not directly covered by a checked-in integration test.

### 9.7 Validation asymmetries that can affect frontend forms

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
- Do not expose admin actions to non-admin users.
- Do not assume lesson viewing requires auth. It is public in the current backend.
- Do not assume course/list responses contain pagination wrappers or enrollment state.
- Do not assume admin CRUD routes use course slug. Admin course edit/delete routes use numeric `courseId`.
- Do not assume the admin upload-init `uploadUrl` is always browser-usable HTTP(S). In local profile it is not.
- Do not assume the backend serves uploaded video files from `http://localhost:7777/media/videos/**`.
- Do not assume login errors distinguish "bad password" from "lead-shell account with no password".
- Do not make locale casing semantically meaningful. Register and enrollment flows normalize differently.
- Do not allow frontend form constraints to be looser than backend register constraints if the same data might later be reused in registration.
- Do not parse `LocalDateTime` strings as timezone-safe UTC unless the frontend deliberately applies a convention.

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
| Admin course routes are slug-based | Admin course get/update/delete use numeric `courseId` | Admin UI calls the wrong endpoints |
| `docs/mvp-schema.sql` is the schema truth | Flyway migrations are canonical | Frontend/admin tooling misses video schema additions |
| `401` only means "missing token" | `401` also covers malformed, expired, and deleted-user tokens | Frontend keeps invalid sessions alive |
| Successful delete responses contain JSON | Admin deletes and upload-complete return `204` no body | Frontend crashes on `response.json()` |
| Local admin upload works end to end in browser | Local upload-init returns `inmemory://...` and no media route exists | Frontend pretends upload/playback are available when they are not |
| All datetime fields are timezone-aware JSON instants | Some are `LocalDateTime` strings without offset | Frontend displays wrong times or mis-sorts |

## 14. Regression-Sensitive Areas

These behaviors should not change casually. If they do change, backend docs and tests must be updated in the same change.

- Public/authenticated/admin route visibility in `SecurityConfig`
- JSON `401` and `403` response shape from security entry points
- JWT filter behavior that reloads users from the database and converts deleted-user tokens into `401`
- Register-upgrade behavior for passwordless lead-shell users
- Generic login `401 Invalid email or password` behavior
- Enrollment duplicate `409` behavior
- Enrollment non-mutation rule for existing nonblank lead-shell profiles
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
