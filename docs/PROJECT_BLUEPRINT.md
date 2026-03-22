# Project Blueprint: Orda Skills Backend Contract Guide

## Snapshot

- Repository in this workspace: `OrdaSkills` / `elearning-backend`
- Repository role: backend source of truth
- Canonical frontend: external `oyn_front` / `jiyuu` React project, not stored in this repo
- Audit date: `2026-03-22`
- Audit method: local backend repo inspection plus user-provided frontend architecture notes
- Runtime verification status: no local rerun of Java or Node tooling from this session because `java`, `mvn`, `node`, and `npm` are not installed in this environment
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
6. admin course CRUD,
7. admin lesson video upload, completion, and delete workflows.

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

| Route | Method | Auth | Purpose | Frontend handling notes |
| --- | --- | --- | --- | --- |
| `/api/health` | GET | Public | Liveness probe | Safe for lightweight connectivity checks |
| `/api/auth/register` | POST | Public | Create account or upgrade a lead-shell user | Expect `201` with auth payload |
| `/api/auth/login` | POST | Public | Exchange credentials for JWT | Expect generic invalid-credentials errors |
| `/api/auth/me` | GET | Authenticated | Return current user snapshot | On `401`, clear stored token and re-auth |
| `/api/courses` | GET | Public | Catalog listing | Use as the source of truth for course cards |
| `/api/courses/{slug}` | GET | Public | Course landing page | Returns the syllabus/details used by landing pages |
| `/api/courses/{courseSlug}/lessons/{lessonSlug}` | GET | Public | Lesson viewer payload | Use `videoUrl` directly for playback |
| `/api/enrollments` | POST | Public | Create enrollment | Supports anonymous lead-shell flow |
| `/api/enrollments` | GET | Authenticated | List enrollments | Students only see their own records |
| `/api/admin/courses` | GET | Admin | List courses | Admin-only CRUD surface |
| `/api/admin/courses/{courseId}` | GET | Admin | Load one course | For edit forms |
| `/api/admin/courses` | POST | Admin | Create course | Expect validation and uniqueness errors |
| `/api/admin/courses/{courseId}` | PUT | Admin | Update course | Same validation model as create |
| `/api/admin/courses/{courseId}` | DELETE | Admin | Delete course if safe | Can return `409` if enrollments exist |
| `/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload` | POST | Admin | Initiate upload | Admin media workflow only |
| `/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete` | POST | Admin | Complete upload | Expects existing uploaded object |
| `/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video` | DELETE | Admin | Delete lesson video | Best-effort object cleanup |

### Frontend-critical response semantics

| Status | Meaning in this backend | Frontend recommendation |
| --- | --- | --- |
| `200` | Read success or login success | Render data or store auth payload |
| `201` | Resource created | Show success feedback and refresh local cache |
| `204` | Delete/complete success with no body | Do not parse JSON |
| `400` | Validation/domain error | Surface message directly in form feedback |
| `401` | Missing, expired, malformed, or deleted-user JWT path | Clear token/session state and redirect or prompt login |
| `403` | Authenticated but insufficient role | Show access denied state |
| `404` | Course, lesson, upload, or resource not found | Render not-found state |
| `409` | Duplicate enrollment or unsafe delete | Show conflict-specific guidance |
| `429` | Rate limit on login/register/enroll | Back off and show retry messaging |

## High-Signal File Map

### Verified backend files in this repo

| Path | Responsibility | Why it matters |
| --- | --- | --- |
| `src/main/java/kz/skills/elearning/config/SecurityConfig.java` | Route authorization and JSON `401/403` behavior | First stop for auth visibility and filter order |
| `src/main/java/kz/skills/elearning/security/RequestRateLimitFilter.java` | Rate limiting for login/register/enrollment | Frontend must respect `429` behavior |
| `src/main/java/kz/skills/elearning/security/JwtAuthenticationFilter.java` | JWT parsing and deleted-user handling | Invalid tokens now degrade to `401` |
| `src/main/java/kz/skills/elearning/service/AuthService.java` | Register/login/current-user lifecycle | Defines lead upgrade and generic login errors |
| `src/main/java/kz/skills/elearning/service/EnrollmentService.java` | Enrollment rules | Defines anonymous enrollment and non-mutation behavior |
| `src/main/java/kz/skills/elearning/controller/AdminCourseController.java` | Admin course CRUD surface | Required backend for the CRUD module |
| `src/main/java/kz/skills/elearning/service/AdminCourseService.java` | Slug uniqueness and delete safety | Governs `409` delete behavior |
| `src/main/java/kz/skills/elearning/controller/AdminLessonVideoController.java` | Admin media endpoints | Entry point for upload-init, upload-complete, and delete |
| `src/main/java/kz/skills/elearning/service/AdminLessonVideoService.java` | Video upload orchestration | Governs admin lesson media behavior |
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
| `src/Pages/CoursePage/CourseCatalog.jsx` | Course catalog UI | Should consume `/api/courses` |
| `src/Pages/CoursePage/CourseLandingPage.jsx` | Course landing and enrollment | Must handle `409` and `429` correctly |
| `src/Pages/LessonPage/LessonViewer.jsx` | Lesson viewer | Must trust backend `videoUrl` |
| `src/redux/userSlice.js` | Session-level Redux state | Likely where `/api/auth/me` should hydrate user state |
| `src/index.css` | Global styles | Important if new auth/conflict states need styling |

## Added and Removed Since The Cleanup Pass

### Added in this backend repo

- admin course CRUD endpoints and service layer
- fail-closed non-local configuration using explicit env vars
- local demo default profile with H2 and in-memory media
- request rate limiting for login, registration, and enrollment
- JSON `401` and `403` responses from security entry points
- safer JWT handling for malformed or deleted-user tokens
- generic login errors that do not reveal passwordless account state
- anonymous enrollment behavior that no longer overwrites existing nonblank lead profiles
- Maven Wrapper scripts
- `.env.example`
- `docs/DEMO_RUNBOOK.md`
- `docs/REQUIREMENTS_MATRIX.md`
- backend regression tests for the new flows

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
   - if future work touches auth, enrollment, lesson payloads, or admin CRUD, compare the change against the reported `oyn_front` architecture and then update this document

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

5. Handle backend statuses intentionally
   - `401`: log out or prompt re-authentication
   - `403`: show permission messaging
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

8. Preserve legacy domains while modernizing eLearning
   - do not break gallery/photoshoots/vacancies while upgrading auth/course flows
   - make changes domain-by-domain instead of rewriting the whole SPA at once

9. Validate happy and failure paths
   - test login, registration, enrollment, lesson viewer, admin CRUD, `401`, `409`, and `429` states
   - if a feature works only on success paths, the integration is incomplete

10. Update docs with code
   - when frontend auth storage, route map, or API wrapper patterns change, update the frontend blueprint or README in the same change

## Instructions And Recommendations For The Frontend AI Agent

Use these rules to avoid major errors and conflicts:

1. Use backend code and tests as the contract source of truth, not memory.
2. Verify whether a feature belongs to the Spring backend (`7777`) or legacy `json-server` (`3001`) before editing.
3. Do not reintroduce client-side password hashing or direct auth against `json-server`.
4. Put API origins in environment variables instead of hardcoding them in components.
5. Use a single API client wrapper for auth headers, JSON parsing, and common error handling.
6. Treat `/api/auth/me` as the canonical session-refresh endpoint.
7. Expect public enrollment to work for anonymous users and registered users alike.
8. Expect duplicate enrollment to return `409`, not silent success.
9. Expect login, registration, and enrollment to be rate limited and capable of returning `429`.
10. Expect malformed, expired, or deleted-user tokens to behave like `401`, not like server crashes.
11. Expect admin-only endpoints under `/api/admin/**` to require an admin role and return `403` for regular learners.
12. Use toast or inline feedback for backend-driven errors, especially `401`, `409`, and `429`.
13. Do not parse bodies on `204` responses.
14. Preserve legacy routes while migrating eLearning flows; avoid all-at-once rewrites unless explicitly requested.
15. If a contract seems unclear, check backend tests before changing frontend assumptions.

## Current State Verified From Source

The following items are directly supported by code in this workspace:

- the backend supports all required MVP backend routes plus admin course CRUD and admin media endpoints
- the backend defaults to a self-contained `local` profile
- non-local runtime expects explicit env vars for PostgreSQL, JWT, and S3-compatible media
- request rate limiting is present for public auth and enrollment endpoints
- lesson viewer payloads expose `videoUrl`
- admin course CRUD is protected by `ROLE_ADMIN`
- the backend test suite includes coverage for:
  - deleted-user JWT fallback to `401`
  - anonymous enrollment non-mutation
  - admin course CRUD
  - `409` delete conflict
  - `429` rate limiting
  - fail-closed postgres profile startup behavior

The following frontend claims come from user-provided architecture notes and were not directly revalidated in this workspace:

- the exact current code shape of `oyn_front`
- React 19 and Router v7 usage in that repo
- `react-toastify` integration details
- the exact hardcoded-origin count in that repo

## Recommended Next Engineering Steps

1. Keep this repo backend-only and do not reintroduce frontend source here unless the architecture is intentionally changed.
2. Reconcile `oyn_front` against this backend contract using this blueprint as the handoff document.
3. Standardize, in the frontend repo:
   - auth storage strategy,
   - env var naming,
   - API client wrapper,
   - error-handling conventions,
   - route ownership between legacy and eLearning domains.
4. Add cross-repo smoke verification once the proper toolchains are available:
   - backend tests,
   - frontend tests,
   - one end-to-end login/enroll/lesson/admin CRUD check.
5. Update this blueprint whenever backend contract or frontend integration strategy changes.

## Mental Model For Future AI Developers

If you remember only one thing, remember this:

- this repo is the backend contract source of truth,
- the frontend lives elsewhere,
- integration work must be deliberate, contract-driven, and cross-repo aware.
