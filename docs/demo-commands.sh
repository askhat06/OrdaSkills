#!/usr/bin/env bash

curl http://localhost:8080/api/courses

echo

echo "--- Enroll test student ---"
curl -X POST http://localhost:8080/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{
    "courseSlug": "digital-skills-kz",
    "fullName": "Test Student",
    "email": "test.student@example.com",
    "locale": "en-KZ"
  }'

echo

echo "--- Open lesson viewer ---"
curl http://localhost:8080/api/courses/digital-skills-kz/lessons/intro-to-digital-skills

echo

echo "--- Show enrollment records ---"
curl http://localhost:8080/api/enrollments
