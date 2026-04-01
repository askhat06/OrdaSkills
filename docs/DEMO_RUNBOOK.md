# Demo Runbook

## Goal

Demonstrate the MVP flow end to end:

1. open the canonical frontend from `oyn_front`
2. enroll a test student
3. log in
4. open the lesson viewer
5. verify course progress for the learner
6. show the saved enrollment and progress in the database

## Start the backend

Default local mode starts with the `local` profile:

- Windows: `.\mvnw.cmd spring-boot:run`
- macOS/Linux: `./mvnw spring-boot:run`

Local defaults:

- URL: `http://localhost:7777`
- DB: H2 in-memory
- H2 console: `http://localhost:7777/h2-console`
- media provider: in-memory

## Start the frontend

Use the canonical frontend from the separate `oyn_front` repository.

Important:

- this backend repo no longer contains frontend source files
- configure the frontend to point at `http://localhost:7777`
- if legacy screens are needed, also start `json-server` for the frontend repo on `3001`

## Demo steps

### 1. Open the landing page

- open the frontend landing page from `oyn_front`
- confirm the seeded course catalog loads from the backend

### 2. Enroll a test student

- fill the enrollment form
- use a test email such as `test.student@example.com`
- submit the form
- confirm the frontend shows success or a handled duplicate-enrollment message

### 3. Log in

- open the frontend login page
- use a registered account
- if demoing admin CRUD, log in with an `ADMIN` user

### 4. Open the lesson viewer

- open the course page
- click the first lesson
- confirm the lesson viewer renders title, metadata, lesson content, and `videoUrl` playback

### 5. Verify course progress

Use the authenticated learner token and verify backend-owned progress state:

```bash
curl -X POST http://localhost:7777/api/progress/courses/digital-skills-kz/start \
  -H "Authorization: Bearer <accessToken>"

curl -X POST http://localhost:7777/api/progress/courses/digital-skills-kz/steps/intro-to-digital-skills/complete \
  -H "Authorization: Bearer <accessToken>"

curl http://localhost:7777/api/progress/courses/digital-skills-kz \
  -H "Authorization: Bearer <accessToken>"
```

Confirm that:

- `status` moves to `IN_PROGRESS` or `COMPLETED` depending on the steps you completed
- `percentComplete` comes from the backend response
- `steps` contains explicit per-lesson progress rows

### 6. Show the stored enrollment and progress in the database

- open `http://localhost:7777/h2-console`
- use:
  - JDBC URL: `jdbc:h2:mem:elearningdb`
  - Username: `sa`
  - Password: blank

Run:

```sql
SELECT e.id, u.full_name, u.email, c.title, e.status, e.enrolled_at
FROM enrollments e
JOIN platform_users u ON e.student_id = u.id
JOIN courses c ON e.course_id = c.id;
```

Then run:

```sql
SELECT cp.id, u.email, c.slug, cp.status, cp.completed_steps, cp.total_steps, cp.percent_complete, cp.attempt_count
FROM course_progress cp
JOIN platform_users u ON cp.student_id = u.id
JOIN courses c ON cp.course_id = c.id;

SELECT cps.progress_id, cps.step_order, l.slug, cps.status, cps.completed_at
FROM course_progress_steps cps
JOIN lessons l ON cps.lesson_id = l.id
ORDER BY cps.progress_id, cps.step_order;
```

## Optional admin demo

If you are using an admin account in the frontend:

1. open the admin course screen
2. create a new course
3. update it
4. preview the public course page
5. delete the course if it has no enrollments

## Expected outcome

The demo is successful when:

- the frontend is interactive against this backend
- the enrollment API persists a student
- login works
- the lesson viewer opens
- learner progress is visible through the backend progress API
- the enrollment and progress records are visible in the database
