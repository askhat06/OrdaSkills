# Demo Runbook

## Goal

Demonstrate the MVP flow end to end:

1. open the canonical frontend from `oyn_front`
2. enroll a test student
3. log in
4. open the lesson viewer
5. show the saved enrollment in the database

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

### 5. Show the stored enrollment in the database

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
- the enrollment record is visible in the database
