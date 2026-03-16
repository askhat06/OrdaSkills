#!/usr/bin/env bash

BASE_URL="http://localhost:7777"

curl "$BASE_URL/api/courses"

echo

echo "--- Enroll test student ---"
curl -X POST "$BASE_URL/api/enrollments" \
  -H "Content-Type: application/json" \
  -d '{
    "courseSlug": "digital-skills-kz",
    "fullName": "Test Student",
    "email": "test.student@example.com",
    "locale": "en-KZ"
  }'

echo

echo "--- Register test student and capture token ---"
TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test Student",
    "email": "test.student@example.com",
    "password": "Password123!",
    "locale": "en-KZ"
  }' | jq -r '.accessToken')

echo "Token acquired: ${TOKEN:+yes}"

echo

echo "--- Open lesson viewer ---"
curl "$BASE_URL/api/courses/digital-skills-kz/lessons/intro-to-digital-skills"

echo

echo "--- Show my enrollment records ---"
curl "$BASE_URL/api/enrollments" \
  -H "Authorization: Bearer $TOKEN"
