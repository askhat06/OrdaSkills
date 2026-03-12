# E-learning Platform MVP Backend

Java Spring Boot backend for the Kazakhstan skills-development MVP.

## Included in this backend

- Course catalog endpoint
- Course landing page endpoint
- Enrollment API with validation and DB persistence
- Lesson viewer endpoint
- H2 in-memory database for demoing enrollment records
- Seeded course and lessons so the frontend has data immediately

## Tech stack

- Java 17+
- Spring Boot 3.5.11
- Spring Web
- Spring Data JPA
- Bean Validation
- H2 database

## Main data model

- `Course`
- `Lesson`
- `PlatformUser`
- `Enrollment`

## API routes

### 1) Course catalog

`GET /api/courses`

### 2) Course landing page

`GET /api/courses/{slug}`

Example:

```bash
curl http://localhost:8080/api/courses/digital-skills-kz
```

### 3) Lesson viewer

`GET /api/courses/{courseSlug}/lessons/{lessonSlug}`

Example:

```bash
curl http://localhost:8080/api/courses/digital-skills-kz/lessons/intro-to-digital-skills
```

### 4) Enrollment API

`POST /api/enrollments`

Example:

```bash
curl -X POST http://localhost:8080/api/enrollments \
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

Optional filters:

- `GET /api/enrollments?courseSlug=digital-skills-kz`
- `GET /api/enrollments?email=test.student@example.com`

## Demo flow

1. Open the course landing page endpoint.
2. Submit the enrollment request for a test student.
3. Open the lesson viewer endpoint.
4. Show the saved enrollment with `GET /api/enrollments`.
5. Optionally open H2 Console at `http://localhost:8080/h2-console`.

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

## Notes for ISC-2 continuation

Natural next additions:

- progress tracking table and endpoints
- localized lesson content per language
- quiz / assessment entities and submission APIs
- instructor CRUD tools for courses and lessons
- authentication and role-based access
