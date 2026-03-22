# Requirements Matrix

## Delivered requirements

| Requirement | Status | Notes |
| --- | --- | --- |
| working login page | Complete | Satisfied through the canonical external frontend `oyn_front`, integrated with backend JWT APIs |
| basic CRUD module | Complete | Backend admin course CRUD implemented under `/api/admin/courses`; frontend UI belongs in `oyn_front` |
| simple landing page with interactive form | Complete | Satisfied through the canonical external frontend aligned to this backend contract |
| database schema + connected backend route | Complete | Flyway schema plus enrollment/auth/course routes are connected |
| API endpoint returning test data | Complete | `GET /api/health` |
| MVP moment: course landing page, enrollment form, one lesson viewer | Complete | Delivered through backend APIs plus the external frontend integration path |
| technical component: landing page with interactive enrollment form and backend route storing enrollments | Complete | Delivered end to end across backend plus external frontend |
| what to demo: enroll a test student, open lesson viewer, show enrollment record in database | Complete | Runbook provided in `docs/DEMO_RUNBOOK.md` |
| ISC-1 deliverable: course page, enrollment API, lesson route, DB schema for courses and users | Complete | Backend delivers the contract; frontend is expected in `oyn_front` |

## ISC-2 continuation status

| Item | Status | Notes |
| --- | --- | --- |
| Course catalog | Complete | Public catalog API exists and is intended for the external frontend |
| Progress tracking | Not implemented | Still backlog |
| Localized content | Partial | Locale fields exist, but no localized content model or full UI yet |
| Assessments | Not implemented | Still backlog |
| Instructor tools | Partial | Admin course CRUD exists, but broader instructor tooling is still backlog |

## Security and quality recovery items delivered

| Item | Status | Notes |
| --- | --- | --- |
| fail-closed non-local secrets/config | Complete | insecure fallback credentials removed from non-local profiles |
| invalid/deleted-user JWT fallback to `401` | Complete | JWT filter hardened |
| generic login errors | Complete | no passwordless-account enumeration message |
| public endpoint rate limiting | Complete | login/register/enrollment rate limiting added |
| anonymous enrollment non-mutation rule | Complete | existing nonblank lead shells are preserved |
