# Project Blueprint: Orda Skills Backend Contract Guide

## Snapshot

- Repository in this workspace: `OrdaSkills` / `elearning-backend`
- Repository role: backend source of truth
- Canonical frontend: external `oyn_front` / `jiyuu` React project, not stored in this repo
- Audit date: `2026-04-15`
- Audit method: local backend repo inspection plus user-provided frontend architecture notes
- Runtime verification status: targeted backend integration coverage for course progress was rerun in this workspace; the external frontend was not rerun here
- Current default backend profile: `local`
- Canonical schema source: `src/main/resources/db/migration/*.sql`
- API contract source of truth: backend controllers, DTOs, security config, YAML profiles, and tests

## Core Rule For Future AI Agents

This repository is backend-only.

Do not assume frontend source code lives here.

If you are working on the frontend:

- use this document as the backend integration guide,
- treat the external `oyn_front` repo as the canonical frontend codebase,
- verify frontend implementation details in that repo before editing.

## The Essence

Orda Skills is a Kazakhstan-focused e-learning MVP whose stable contract now lives in this Spring Boot backend.

The backend currently provides:

1. JWT register/login/current-user flows,
2. public course catalog and course landing endpoints,
3. lesson viewer payloads including `videoUrl`,
4. public enrollment creation with lead-shell support,
5. authenticated enrollment listing,
6. authenticated course progress tracking with explicit lesson steps,
7. admin course CRUD with moderation (publish/reject) workflows,
8. admin lesson video upload, completion, and delete workflows,
9. teacher course and lesson management with an ownership-gated publication lifecycle.

The backend supports three user roles: `STUDENT`, `TEACHER`, and `ADMIN`.

The frontend is an external concern:

- the real learner-facing UI is expected to live in `oyn_front`,
- legacy gallery/photoshoots/vacancy flows remain a frontend-side concern tied to `json-server`,
- this repo should document frontend integration, not host the frontend itself.

## Architecture and Integration Boundaries

### Backend architecture in this repo

The backend is a layered Spring monolith:

- `controller` exposes HTTP routes
- `service` applies business rules and assembles DTOs
- `repository` handles persistence access
- `entity` models the schema
- `security` manages JWT auth and request filtering
- `config` manages profiles, CORS, rate limiting, and media wiring
- `service/video` abstracts media storage providers
- `exception` keeps API errors consistent

### External frontend boundary

Based on the user-provided blueprint, the external frontend:

- uses React plus Router, Redux, and Context,
- talks to this backend on `http://localhost:7777`,
- still talks to `json-server` on `http://localhost:3001` for legacy domains,
- must preserve legacy domains while aligning eLearning flows to this backend contract.

Important consequence:

- when backend and frontend seem to disagree, this backend repo is the contract source of truth for eLearning behavior,
- but frontend implementation details must still be verified in `oyn_front` before making assumptions about routing, storage, or state architecture.

## API Surface That Frontends Must Respect

### Public and learner routes

| Route | Method | Auth | Purpose | Frontend handling notes |
| --- | --- | --- | --- | --- |
| `/api/health` | GET | Public | Liveness probe | Safe for lightweight connectivity checks |
| `/api/auth/register` | POST | Public | Create account or upgrade a lead-shell user | Expect `201` with auth payload; rate-limited |
| `/api/auth/login` | POST | Public | Exchange credentials for JWT | Expect generic invalid-credentials errors; rate-limited |
| `/api/auth/me` | GET | Authenticated | Return current user snapshot | On `401`, clear stored token and re-auth |
| `/api/courses` | GET | Public | Catalog listing (PUBLISHED only) | Use as the source of truth for course cards |
| `/api/courses/{slug}` | GET | Public | Course landing page | Enrolled users can see non-PUBLISHED courses; principal is optional |
| `/api/courses/{courseSlug}/lessons/{lessonSlug}` | GET | Public | Lesson viewer payload | Use `videoUrl` directly for playback; enrolled users bypass PUBLISHED check |
| `/api/enrollments` | POST | Public | Create enrollment | Supports anonymous lead-shell flow; rate-limited; `201` on success |
| `/api/enrollments` | GET | Authenticated | List enrollments | Supports `?courseSlug=` and `?email=` filters; students see their own records |
| `/api/progress/courses/{courseSlug}` | GET | Authenticated + enrolled | Read course progress | Returns backend-owned progress snapshot |
| `/api/progress/courses/{courseSlug}/start` | POST | Authenticated + enrolled | Start/resume tracked attempt | Increments attempt count only for new attempts |
| `/api/progress/courses/{courseSlug}/current-step` | PUT | Authenticated + enrolled | Persist active lesson focus | Expects `{ "lessonSlug": "..." }` |
| `/api/progress/courses/{courseSlug}/steps/{lessonSlug}/complete` | POST | Authenticated + enrolled | Complete one progress step | Recalculates counters and percent |
| `/api/progress/courses/{courseSlug}/complete` | POST | Authenticated + enrolled | Complete whole course | Supports zero-step completion |
| `/api/progress/courses/{courseSlug}/reset` | POST | Authenticated + enrolled | Reset progress | Prepares next attempt without incrementing count yet |

### Admin routes (`ROLE_ADMIN` required)

| Route | Method | Auth | Purpose | Frontend handling notes |
| --- | --- | --- | --- | --- |
| `/api/admin/courses` | GET | Admin | List all courses (all statuses) | Admin-only CRUD surface |
| `/api/admin/courses/pending` | GET | Admin | Courses awaiting review | Moderation queue feed |
| `/api/admin/courses/{courseId}` | GET | Admin | Load one course by ID | For edit and review forms |
| `/api/admin/courses` | POST | Admin | Create course (admin-owned) | Expect validation and uniqueness errors; `201` on success |
| `/api/admin/courses/{courseId}` | PUT | Admin | Update course | Same validation model as create |
| `/api/admin/courses/{courseId}` | DELETE | Admin | Delete course if safe | Can return `409` if enrollments exist; `204` on success |
| `/api/admin/courses/{courseId}/publish` | POST | Admin | Approve PENDING_REVIEW → PUBLISHED | Returns `400` if course is not in PENDING_REVIEW |
| `/api/admin/courses/{courseId}/reject` | POST | Admin | Reject PENDING_REVIEW → REJECTED | Body: `{ "reason": "..." }` (required); `400` if not in PENDING_REVIEW |
| `/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload` | POST | Admin | Initiate video upload | Admin media workflow only |
| `/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete` | POST | Admin | Complete video upload | Expects existing uploaded object; `204` on success |
| `/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video` | DELETE | Admin | Delete lesson video | Best-effort object cleanup; `204` on success |

### Teacher routes (`ROLE_TEACHER` required)

| Route | Method | Auth | Purpose | Frontend handling notes |
| --- | --- | --- | --- | --- |
| `/api/teacher/courses` | GET | Teacher | List teacher's own courses (all statuses) | Scoped to calling teacher by ownership |
| `/api/teacher/courses/{courseSlug}` | GET | Teacher | Load one owned course | Returns `403` if teacher does not own it |
| `/api/teacher/courses` | POST | Teacher | Create course in DRAFT | `201` on success; course is not visible publicly until published |
| `/api/teacher/courses/{courseSlug}` | PUT | Teacher | Update course metadata | Only allowed in DRAFT or REJECTED status |
| `/api/teacher/courses/{courseSlug}` | DELETE | Teacher | Delete DRAFT course with no enrollments | `409` if enrollments exist; `204` on success |
| `/api/teacher/courses/{courseSlug}/submit` | POST | Teacher | DRAFT or REJECTED → PENDING_REVIEW | Locks the course for admin review |
| `/api/teacher/courses/{courseSlug}/withdraw` | POST | Teacher | PENDING_REVIEW → DRAFT | Pulls course back before admin acts |
| `/api/teacher/courses/{courseSlug}/lessons` | GET | Teacher | List lessons for owned course | Returns ordered lesson list |
| `/api/teacher/courses/{courseSlug}/lessons` | POST | Teacher | Create lesson | `201` on success |
| `/api/teacher/courses/{courseSlug}/lessons/{lessonSlug}` | PUT | Teacher | Update lesson | Ownership-checked |
| `/api/teacher/courses/{courseSlug}/lessons/{lessonSlug}` | DELETE | Teacher | Delete lesson | `204` on success |
| `/api/teacher/courses/{courseSlug}/lessons/{lessonSlug}/video-upload` | POST | Teacher | Initiate video upload for owned lesson | Ownership-checked; delegates to shared video service |
| `/api/teacher/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete` | POST | Teacher | Complete video upload | Ownership-checked; `204` on success |
| `/api/teacher/courses/{courseSlug}/lessons/{lessonSlug}/video` | DELETE | Teacher | Delete lesson video | Ownership-checked; `204` on success |

### Course status lifecycle

```
DRAFT ──(submit)──► PENDING_REVIEW ──(publish)──► PUBLISHED
  ▲                       │
  │        (withdraw)     │ (reject, with reason)
  │◄──────────────────────┤
  │                       ▼
  └──────────────── REJECTED
```

- `DRAFT`: teacher is building; not visible in the public catalog.
- `PENDING_REVIEW`: submitted for admin review; locked for teacher edits.
- `PUBLISHED`: admin-approved; visible in `/api/courses` catalog.
- `REJECTED`: admin rejected with a mandatory reason; teacher can edit and resubmit.
- Admin-created and seeded courses default to `PUBLISHED` (V4 migration default).

### Frontend-critical response semantics

| Status | Meaning in this backend | Frontend recommendation |
| --- | --- | --- |
| `200` | Read success or login success | Render data or store auth payload |
| `201` | Resource created | Show success feedback and refresh local cache |
| `204` | Delete/complete success with no body | Do not parse JSON |
| `400` | Validation/domain error (e.g., wrong course status for action) | Surface message directly in form feedback |
| `401` | Missing, expired, malformed, or deleted-user JWT | Clear token/session state and redirect or prompt login |
| `403` | Authenticated but insufficient role or ownership mismatch | Show access denied state |
| `404` | Course, lesson, upload, or resource not found | Render not-found state |
| `409` | Duplicate enrollment or unsafe delete | Show conflict-specific guidance |
| `429` | Rate limit on login/register/enroll | Back off and show retry messaging |

## High-Signal File Map

### Verified backend files in this repo

| Path | Responsibility | Why it matters |
| --- | --- | --- |
| `src/main/java/kz/skills/elearning/config/SecurityConfig.java` | Route authorization and JSON `401/403` behavior | First stop for auth visibility and filter order; `/api/admin/**` → ADMIN, `/api/teacher/**` → TEACHER |
| `src/main/java/kz/skills/elearning/security/RequestRateLimitFilter.java` | Rate limiting for login/register/enrollment | Frontend must respect `429` behavior; limits only POST on those three paths |
| `src/main/java/kz/skills/elearning/security/JwtAuthenticationFilter.java` | JWT parsing and deleted-user handling | Invalid tokens degrade silently to unauthenticated; `401` is emitted downstream by security entry point |
| `src/main/java/kz/skills/elearning/service/AuthService.java` | Register/login/current-user lifecycle | Defines lead upgrade and generic login errors |
| `src/main/java/kz/skills/elearning/service/EnrollmentService.java` | Enrollment rules | Defines anonymous enrollment and non-mutation behavior |
| `src/main/java/kz/skills/elearning/controller/ProgressController.java` | Progress API surface | Exact course-progress route contract |
| `src/main/java/kz/skills/elearning/service/ProgressService.java` | Progress lifecycle orchestration | Backend source of truth for counters, attempts, and step sync |
| `src/main/java/kz/skills/elearning/controller/AdminCourseController.java` | Admin course CRUD + moderation surface | Includes pending list, publish, and reject endpoints |
| `src/main/java/kz/skills/elearning/service/AdminCourseService.java` | Slug uniqueness, delete safety, and moderation | Governs `409` delete behavior and status-transition guards |
| `src/main/java/kz/skills/elearning/controller/TeacherCourseController.java` | Teacher course CRUD + lifecycle surface | Create/edit in DRAFT, submit, withdraw |
| `src/main/java/kz/skills/elearning/service/TeacherCourseService.java` | Teacher ownership checks and status transitions | Guards edits to DRAFT/REJECTED only; `CourseOwnershipGuard` enforces owner match |
| `src/main/java/kz/skills/elearning/controller/TeacherLessonController.java` | Teacher lesson CRUD + video ops | Delegates video logic to `AdminLessonVideoService` after ownership check |
| `src/main/java/kz/skills/elearning/service/TeacherLessonService.java` | Teacher lesson management | Lesson create/update/delete and ownership enforcement for video ops |
| `src/main/java/kz/skills/elearning/service/CourseOwnershipGuard.java` | Ownership enforcement utility | Throws `403` when teacher is not course owner |
| `src/main/java/kz/skills/elearning/controller/AdminLessonVideoController.java` | Admin media endpoints | Entry point for admin video upload-init, upload-complete, and delete |
| `src/main/java/kz/skills/elearning/service/AdminLessonVideoService.java` | Video upload orchestration | Shared by admin and teacher video endpoints |
| `src/main/java/kz/skills/elearning/entity/CourseStatus.java` | Course publication status enum | DRAFT, PENDING_REVIEW, PUBLISHED, REJECTED |
| `src/main/java/kz/skills/elearning/entity/UserRole.java` | User role enum | STUDENT, TEACHER, ADMIN |
| `src/main/resources/db/migration/V3__add_course_progress_tracking.sql` | Progress persistence contract | Defines aggregate and per-step schema |
| `src/main/resources/db/migration/V4__add_teacher_role_and_course_status.sql` | Teacher ownership and course status schema | Adds `owner_id`, `status`, `rejection_reason` to `courses` |
| `src/main/resources/application.yml` | Shared config | Source of truth for profile default and rate limits |
| `src/main/resources/application-local.yml` | Local demo config | H2 + in-memory media + local JWT secret |
| `src/main/resources/application-postgres.yml` | Non-local env-driven config | Fail-closed runtime config for PostgreSQL, S3, and JWT |
| `src/test/java/kz/skills/elearning/ApiIntegrationTests.java` | Main backend regression suite | Best contract reference when frontend behavior is unclear |
| `src/test/java/kz/skills/elearning/RateLimitingIntegrationTests.java` | `429` regression suite | Frontend backoff behavior should match this |
| `src/test/java/kz/skills/elearning/ProfileConfigurationTests.java` | Fail-closed config test | Confirms non-local secret strictness |

### External frontend map from user-provided blueprint

These paths are not in this repo and should be revalidated inside `oyn_front` before editing:

| Path | Reported responsibility | Why it matters |
| --- | --- | --- |
| `src/App.js` | Main router and provider tree | Likely the composition root for frontend integration work |
| `src/Components/Login.jsx` | JWT acquisition and storage | Must align with the current backend contract |
| `src/Components/Registration.jsx` | Registration flow | Must send the correct Spring DTO shape |
| `src/Pages/CoursePage/CourseCatalog.jsx` | Course catalog UI | Should consume `/api/courses` (PUBLISHED only) |
| `src/Pages/CoursePage/CourseLandingPage.jsx` | Course landing and enrollment | Must handle `409` and `429` correctly |
| `src/Pages/LessonPage/LessonViewer.jsx` | Lesson viewer | Must trust backend `videoUrl` |
| `src/redux/userSlice.js` | Session-level Redux state | Likely where `/api/auth/me` should hydrate user state and role |
| `src/index.css` | Global styles | Important if new auth/conflict states need styling |

## Added and Removed Since The Cleanup Pass

### Added in this backend repo

- admin course CRUD endpoints and service layer
- fail-closed non-local configuration using explicit env vars
- local demo default profile with H2 and in-memory media
- request rate limiting for login, registration, and enrollment
- course progress tracking with explicit step rows, reset support, repeat attempts, and zero-step completion
- JSON `401` and `403` responses from security entry points
- safer JWT handling for malformed or deleted-user tokens
- generic login errors that do not reveal passwordless account state
- anonymous enrollment behavior that no longer overwrites existing nonblank lead profiles
- Maven Wrapper scripts
- `.env.example`
- `docs/DEMO_RUNBOOK.md`
- `docs/REQUIREMENTS_MATRIX.md`
- backend regression tests for the new flows
- **TEACHER role** with full course and lesson management lifecycle
- **course status lifecycle**: DRAFT → PENDING_REVIEW → PUBLISHED / REJECTED
- **admin moderation endpoints**: pending queue, publish, reject (with mandatory reason)
- **teacher video upload**: teachers can manage videos for their own courses via the same video service
- **`CourseOwnershipGuard`** utility enforcing that teachers can only act on their own courses
- **V4 database migration**: `owner_id`, `status`, `rejection_reason` columns on `courses`

### Removed from this repo to eliminate architectural confusion

- the temporary in-repo frontend folder that I had added earlier
- all documentation claims that this repo stores the canonical frontend
- all README/blueprint references telling future agents to edit a local frontend here

### Invalidated assumptions

- the backend no longer defaults to `postgres`; it now defaults to `local`
- committed fallback secrets for non-local runtime are no longer the intended behavior
- login no longer tells passwordless users to register first; it now returns a generic invalid-credentials error
- anonymous enrollment should not be treated as a profile-update mechanism for existing lead shells
- `docs/mvp-schema.sql` is not the schema source of truth
- frontend implementation details must be looked up in `oyn_front`, not inferred from this repo
- there are now **three roles** (`STUDENT`, `TEACHER`, `ADMIN`), not two; frontend role-based routing must account for `TEACHER`
- course visibility in the public catalog is now gated on `PUBLISHED` status; admin-created/seeded courses have `PUBLISHED` as their migration default

## Backend-Side Conflict Resolution Plan

This is the plan the backend-side AI agent should use to find and fix conflicts on this side of the integration.

1. Establish source-of-truth precedence
   - backend code and tests win over stale docs or stale frontend assumptions
   - use controllers, DTOs, `SecurityConfig`, YAML config, and integration tests as the contract authority

2. Track frontend-relevant behavior changes explicitly
   - when route auth, status codes, DTO fields, or profile behavior changes, update this blueprint and backend tests in the same pass

3. Keep error semantics stable
   - avoid casual changes to `401`, `403`, `409`, and `429` behavior without updating contract notes and regression tests

4. Prefer compatibility guidance before risky breaks
   - if the frontend lags behind, document the integration impact before introducing shape-breaking changes

5. Reconcile external frontend assumptions before major refactors
   - if future work touches auth, enrollment, lesson payloads, admin CRUD, or teacher flows, compare the change against the reported `oyn_front` architecture and then update this document

### Implemented backend-side cleanup in this session

- the temporary in-repo frontend was removed
- stale frontend references were removed from the backend docs
- this blueprint now treats `oyn_front` as the canonical frontend
- the backend contract and frontend-agent guidance now live in one document

## Conflict Resolution Plan For The Frontend AI Agent

Use this plan when `oyn_front` is behind the backend or when backend/frontend assumptions conflict.

1. Audit before editing
   - inspect `App.js`, auth components, course pages, Redux session slice, and shared fetch helpers first
   - identify whether the code path talks to `7777` or `3001`
   - do not assume every failing screen belongs to the eLearning backend

2. Rebuild the contract map from backend source of truth
   - verify routes, payloads, auth requirements, and failure codes against backend controllers plus integration tests
   - never infer endpoint shape from stale frontend code alone

3. Centralize network behavior early
   - move hardcoded origins into `.env`
   - create one API client that:
     - chooses the correct backend origin
     - attaches the Bearer token automatically for protected `7777` calls
     - normalizes JSON and non-JSON errors
     - handles `204` correctly

4. Standardize session lifecycle
   - choose one browser storage strategy and apply it consistently
   - after login or registration, store the token and refresh session state using `/api/auth/me` if needed
   - on `401`, clear the stored token and session state immediately
   - read the `role` field from the auth response or `/api/auth/me` to gate teacher and admin UI sections

5. Handle backend statuses intentionally
   - `401`: log out or prompt re-authentication
   - `403`: show permission messaging (also raised when a teacher accesses another teacher's course)
   - `404`: render missing-course or missing-lesson states
   - `409`: surface duplicate-enrollment or delete-conflict guidance
   - `429`: show retry/backoff messaging and avoid spam retries

6. Keep the lead-shell model intact
   - do not force registration before enrollment
   - do not assume enrollment should update an existing user profile
   - treat enrollment as a low-friction funnel first

7. Integrate admin flows separately from learner flows
   - guard admin routes in the router and UI state
   - do not leak admin actions into learner components
   - expect admin CRUD and admin media endpoints to require `ROLE_ADMIN`

8. Integrate teacher flows as a distinct role surface
   - gate teacher routes with `ROLE_TEACHER` checks in the frontend router
   - teacher dashboard shows own courses (all statuses) via `/api/teacher/courses`
   - respect the course status lifecycle: only allow editing in DRAFT or REJECTED status
   - surface rejection reason from the course response when status is `REJECTED`
   - provide submit / withdraw actions tied to the correct status preconditions

9. Preserve legacy domains while modernizing eLearning
   - do not break gallery/photoshoots/vacancies while upgrading auth/course flows
   - make changes domain-by-domain instead of rewriting the whole SPA at once

10. Validate happy and failure paths
    - test login, registration, enrollment, lesson viewer, admin CRUD, teacher course lifecycle, `401`, `409`, and `429` states
    - if a feature works only on success paths, the integration is incomplete

11. Update docs with code
    - when frontend auth storage, route map, or API wrapper patterns change, update the frontend blueprint or README in the same change

## Instructions And Recommendations For The Frontend AI Agent

Use these rules to avoid major errors and conflicts:

1. Use backend code and tests as the contract source of truth, not memory.
2. Verify whether a feature belongs to the Spring backend (`7777`) or legacy `json-server` (`3001`) before editing.
3. Do not reintroduce client-side password hashing or direct auth against `json-server`.
4. Put API origins in environment variables instead of hardcoding them in components.
5. Use a single API client wrapper for auth headers, JSON parsing, and common error handling.
6. Treat `/api/auth/me` as the canonical session-refresh endpoint; it also returns the user's role.
7. Expect public enrollment to work for anonymous users and registered users alike.
8. Expect duplicate enrollment to return `409`, not silent success.
9. Expect login, registration, and enrollment to be rate limited and capable of returning `429`.
10. Expect malformed, expired, or deleted-user tokens to behave like `401`, not like server crashes.
11. Expect admin-only endpoints under `/api/admin/**` to require `ROLE_ADMIN` and return `403` for others.
12. Expect teacher-only endpoints under `/api/teacher/**` to require `ROLE_TEACHER` and return `403` for others; a teacher accessing another teacher's course also receives `403`.
13. Use toast or inline feedback for backend-driven errors, especially `401`, `403`, `409`, and `429`.
14. Do not parse bodies on `204` responses.
15. Preserve legacy routes while migrating eLearning flows; avoid all-at-once rewrites unless explicitly requested.
16. If a contract seems unclear, check backend tests before changing frontend assumptions.
17. Treat course progress, step completion, and percent calculation as backend-owned state, not as frontend-derived truth.
18. Show course status (`DRAFT`, `PENDING_REVIEW`, `PUBLISHED`, `REJECTED`) in the teacher dashboard; surface `rejectionReason` when status is `REJECTED`.
19. Only render the public course catalog from `/api/courses`; that endpoint returns only `PUBLISHED` courses — do not filter client-side.

## Current State Verified From Source

The following items are directly supported by code in this workspace:

- the backend supports all required MVP backend routes plus admin course CRUD, admin media endpoints, and teacher course/lesson management
- the backend exposes authenticated course progress routes under `/api/progress/courses/**`
- enrollment bootstraps course progress and per-lesson progress-step rows
- progress percent is calculated on the backend from persisted step state
- progress steps stay synchronized with the current lesson set for a course
- the backend defaults to a self-contained `local` profile
- non-local runtime expects explicit env vars for PostgreSQL, JWT, and S3-compatible media
- request rate limiting is present for public auth and enrollment endpoints (POST only)
- lesson viewer payloads expose `videoUrl`
- admin course CRUD and moderation (publish/reject) are protected by `ROLE_ADMIN`
- teacher course and lesson CRUD plus the publication lifecycle are protected by `ROLE_TEACHER`
- teacher ownership is enforced at the service layer via `CourseOwnershipGuard`; unauthorized access returns `403`
- course status transitions are guarded server-side: edits only in DRAFT/REJECTED, submissions only from DRAFT/REJECTED, publish/reject only from PENDING_REVIEW
- admin-created and seeded courses default to `PUBLISHED` status via the V4 migration default
- V4 database migration adds `owner_id`, `status`, and `rejection_reason` columns to the `courses` table
- three user roles are defined: `STUDENT`, `TEACHER`, `ADMIN`
- JSON `401` and `403` responses are emitted by security entry points in `SecurityConfig`
- the backend test suite includes coverage for:
  - deleted-user JWT fallback to `401`
  - anonymous enrollment non-mutation
  - course progress bootstrap, reset, repeat attempts, and zero-step completion
  - admin course CRUD
  - `409` delete conflict
  - `429` rate limiting
  - fail-closed postgres profile startup behavior

The following frontend claims come from user-provided architecture notes and were not directly revalidated in this workspace:

- the exact current code shape of `oyn_front`
- React 19 and Router v7 usage in that repo
- `react-toastify` integration details
- the exact hardcoded-origin count in that repo
- whether the frontend currently handles `ROLE_TEACHER` routing or the course status lifecycle

## Recommended Next Engineering Steps

1. Keep this repo backend-only and do not reintroduce frontend source here unless the architecture is intentionally changed.
2. Reconcile `oyn_front` against this backend contract using this blueprint as the handoff document.
3. Standardize, in the frontend repo:
   - auth storage strategy,
   - env var naming,
   - API client wrapper,
   - error-handling conventions,
   - role-based route guards for `STUDENT`, `TEACHER`, and `ADMIN`,
   - teacher dashboard integrating the course status lifecycle.
4. Add cross-repo smoke verification once the proper toolchains are available:
   - backend tests,
   - frontend tests,
   - one end-to-end login/enroll/lesson/admin CRUD/teacher submit-review check.
5. Update this blueprint whenever backend contract or frontend integration strategy changes.

## Mental Model For Future AI Developers

If you remember only one thing, remember this:

- this repo is the backend contract source of truth,
- the frontend lives elsewhere,
- integration work must be deliberate, contract-driven, and cross-repo aware,
- there are three roles (`STUDENT`, `TEACHER`, `ADMIN`); the teacher surface lives under `/api/teacher/**` and is separate from admin.
