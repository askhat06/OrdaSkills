# E-learning Platform MVP Backend

Java Spring Boot backend for the Kazakhstan skills-development MVP.

## Included in this backend

- Course catalog endpoint
- Course landing page endpoint
- Enrollment API with validation and DB persistence
- Lesson viewer endpoint
- JWT-based registration, login, and current-user endpoint
- H2 in-memory database for demoing enrollment records
- Seeded course and lessons so the frontend has data immediately

## Tech stack

- Java 17+
- Spring Boot 3.5.11
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Security
- JWT (JJWT)
- H2 database
- PostgreSQL profile for persistent storage

## Main data model

- `Course`
- `Lesson`
- `PlatformUser`
- `Enrollment`

## API routes

### 1) Course catalog

`GET /api/courses`

### 0) Health check

`GET /api/health`

### Auth

#### Register

`POST /api/auth/register`

Example:

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

#### Login

`POST /api/auth/login`

#### Current user

`GET /api/auth/me`

### 2) Course landing page

`GET /api/courses/{slug}`

Example:

```bash
curl http://localhost:7777/api/courses/digital-skills-kz
```

### 3) Lesson viewer

`GET /api/courses/{courseSlug}/lessons/{lessonSlug}`

Example:

```bash
curl http://localhost:7777/api/courses/digital-skills-kz/lessons/intro-to-digital-skills
```

### 4) Enrollment API

`POST /api/enrollments`

Example:

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

### 5) View enrollment records

`GET /api/enrollments`

Authentication required. Students can view only their own enrollments. Admins can view all enrollments and use filters.

Optional admin filters:

- `GET /api/enrollments?courseSlug=digital-skills-kz`
- `GET /api/enrollments?email=test.student@example.com`
- `GET /api/enrollments?courseSlug=digital-skills-kz&email=test.student@example.com`

## Demo flow

1. Open the course landing page endpoint.
2. Submit the enrollment request for a test student.
3. Register or log in to obtain a JWT.
4. Open the lesson viewer endpoint.
5. Show the saved enrollment with authenticated `GET /api/enrollments`.
6. Optionally open H2 Console at `http://localhost:7777/h2-console`.

H2 connection values:

- JDBC URL: `jdbc:h2:mem:elearningdb`
- Username: `sa`
- Password: *(blank)*

Example query in H2:

```sql
SELECT e.id, u.full_name, u.email, c.title, e.status, e.enrolled_at
FROM enrollments e
JOIN platform_users u ON e.student_id = u.id
JOIN courses c ON e.course_id = c.id;
```

## Run locally

Import the project into IntelliJ IDEA or another Java IDE, then run `ElearningBackendApplication`.

Or run with Maven installed globally:

```bash
mvn spring-boot:run
```

Default local URL:

- `http://localhost:7777`

## Current implementation notes

- The app uses a seeded course so the demo works on first start.
- JWT expiration is configured via `app.security.jwt.expiration` and defaults to `24h`.
- Anonymous enrollment can create a user shell that is later upgraded during registration with the same email.

## Natural next additions

- progress tracking table and endpoints
- localized lesson content per language
- quiz / assessment entities and submission APIs
- instructor CRUD tools for courses and lessons
- stronger production hardening for secrets, H2 console exposure, and deployment automation
