package kz.skills.elearning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.skills.elearning.entity.Lesson;
import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.entity.UserRole;
import kz.skills.elearning.repository.LessonRepository;
import kz.skills.elearning.repository.PlatformUserRepository;
import kz.skills.elearning.service.video.InMemoryVideoStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:apitests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.security.jwt.secret=c3VwZXItc2VjcmV0LWtleS1mb3ItaW50ZWdyYXRpb24tdGVzdHM=",
        "app.media.video.provider=in-memory",
        "app.media.video.public-base-url=https://cdn.example.test/videos",
        "app.media.video.max-file-size-bytes=2048"
})
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTests {

    private static final String COURSE_SLUG = "digital-skills-kz";
    private static final String LESSON_SLUG = "intro-to-digital-skills";
    private static final String DEFAULT_PASSWORD = "Password123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatformUserRepository platformUserRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InMemoryVideoStorageService inMemoryVideoStorageService;

    @BeforeEach
    void setUp() {
        inMemoryVideoStorageService.clear();
    }

    @Test
    void registerLoginAndMeFlowReturnsConfiguredJwtLifetime() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fullName", "Student One",
                                "email", "student.one@example.com",
                                "password", DEFAULT_PASSWORD,
                                "locale", "en-KZ"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(86400))
                .andExpect(jsonPath("$.user.email").value("student.one@example.com"))
                .andReturn();

        String token = readAccessToken(registerResult);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student.one@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "student.one@example.com",
                                "password", DEFAULT_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresInSeconds").value(86400))
                .andExpect(jsonPath("$.user.fullName").value("Student One"));
    }

    @Test
    void unauthenticatedUserCannotListEnrollments() throws Exception {
        mockMvc.perform(get("/api/enrollments"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void publicRoutesAreAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value(COURSE_SLUG));

        mockMvc.perform(get("/api/courses/{slug}", COURSE_SLUG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(COURSE_SLUG));

        mockMvc.perform(get("/api/courses/{courseSlug}/lessons/{lessonSlug}", COURSE_SLUG, LESSON_SLUG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessonSlug").value(LESSON_SLUG))
                .andExpect(jsonPath("$.videoUrl").value("https://example.com/videos/intro-to-digital-skills"));
    }

    @Test
    void protectedAuthMeRequiresToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void studentCanSeeOnlyOwnEnrollments() throws Exception {
        createEnrollment("Student One", "student.one@example.com", "en-KZ");
        createEnrollment("Student Two", "student.two@example.com", "ru-KZ");

        String studentToken = registerAndGetToken("Student One", "student.one@example.com", "en-KZ");

        mockMvc.perform(get("/api/enrollments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("student.one@example.com"));

        mockMvc.perform(get("/api/enrollments")
                        .param("courseSlug", COURSE_SLUG)
                        .header(HttpHeaders.AUTHORIZATION, bearer(studentToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].courseSlug").value(COURSE_SLUG));

        mockMvc.perform(get("/api/enrollments")
                        .param("email", "student.two@example.com")
                        .header(HttpHeaders.AUTHORIZATION, bearer(studentToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Students can only view their own enrollments"));
    }

    @Test
    void adminCanFilterEnrollmentsAcrossUsers() throws Exception {
        createEnrollment("Student One", "student.one@example.com", "en-KZ");
        createEnrollment("Student Two", "student.two@example.com", "ru-KZ");
        createAdmin("admin@example.com", "Platform Admin");

        String adminToken = loginAndGetToken("admin@example.com", DEFAULT_PASSWORD);

        mockMvc.perform(get("/api/enrollments")
                        .param("courseSlug", COURSE_SLUG)
                        .param("email", "student.two@example.com")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("student.two@example.com"));
    }

    @Test
    void duplicateEnrollmentDoesNotMutateExistingLeadProfile() throws Exception {
        createEnrollment("Original Name", "lead@example.com", "en-KZ");

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "courseSlug", COURSE_SLUG,
                                "fullName", "Mutated Name",
                                "email", "lead@example.com",
                                "locale", "ru-KZ"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Student already enrolled in course: " + COURSE_SLUG));

        PlatformUser lead = platformUserRepository.findByEmailIgnoreCase("lead@example.com").orElseThrow();
        assertThat(lead.getFullName()).isEqualTo("Original Name");
        assertThat(lead.getLocale()).isEqualTo("en-KZ");
    }

    @Test
    void adminCanInitiateAndCompleteVideoUpload() throws Exception {
        createAdmin("admin@example.com", "Platform Admin");
        String adminToken = loginAndGetToken("admin@example.com", DEFAULT_PASSWORD);

        MvcResult initResult = mockMvc.perform(post("/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload", COURSE_SLUG, LESSON_SLUG)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fileName", "lesson.mp4",
                                "contentType", "video/mp4",
                                "sizeBytes", 1024
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiredHeaders.Content-Type").value("video/mp4"))
                .andExpect(jsonPath("$.playbackUrl").value(org.hamcrest.Matchers.containsString("https://cdn.example.test/videos/")))
                .andReturn();

        String objectKey = objectMapper.readTree(initResult.getResponse().getContentAsString()).get("objectKey").asText();
        inMemoryVideoStorageService.putObject(objectKey, "video/mp4", 1024);

        mockMvc.perform(post("/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete", COURSE_SLUG, LESSON_SLUG)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("objectKey", objectKey))))
                .andExpect(status().isNoContent());

        Lesson lesson = lessonRepository.findByCourse_SlugAndSlug(COURSE_SLUG, LESSON_SLUG).orElseThrow();
        assertThat(lesson.getVideoStorageKey()).isEqualTo(objectKey);
        assertThat(lesson.getVideoOriginalFilename()).isEqualTo("lesson.mp4");
        assertThat(lesson.getVideoContentType()).isEqualTo("video/mp4");
        assertThat(lesson.getVideoSizeBytes()).isEqualTo(1024L);

        mockMvc.perform(get("/api/courses/{courseSlug}/lessons/{lessonSlug}", COURSE_SLUG, LESSON_SLUG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.videoUrl").value("https://cdn.example.test/videos/" + objectKey));
    }

    @Test
    void adminVideoUploadEndpointsRequireAdminRole() throws Exception {
        String studentToken = registerAndGetToken("Student One", "student.one@example.com", "en-KZ");

        mockMvc.perform(post("/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload", COURSE_SLUG, LESSON_SLUG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fileName", "lesson.mp4",
                                "contentType", "video/mp4",
                                "sizeBytes", 512
                        ))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload", COURSE_SLUG, LESSON_SLUG)
                        .header(HttpHeaders.AUTHORIZATION, bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fileName", "lesson.mp4",
                                "contentType", "video/mp4",
                                "sizeBytes", 512
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidVideoUploadRequestsAreRejected() throws Exception {
        createAdmin("admin@example.com", "Platform Admin");
        String adminToken = loginAndGetToken("admin@example.com", DEFAULT_PASSWORD);

        mockMvc.perform(post("/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload", COURSE_SLUG, LESSON_SLUG)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fileName", "lesson.mov",
                                "contentType", "video/quicktime",
                                "sizeBytes", 512
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported video content type: video/quicktime"));

        mockMvc.perform(post("/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload", COURSE_SLUG, LESSON_SLUG)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fileName", "large.mp4",
                                "contentType", "video/mp4",
                                "sizeBytes", 4096
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Video exceeds max file size of 2048 bytes"));
    }

    @Test
    void uploadCompletionFailsWhenObjectIsMissing() throws Exception {
        createAdmin("admin@example.com", "Platform Admin");
        String adminToken = loginAndGetToken("admin@example.com", DEFAULT_PASSWORD);

        MvcResult initResult = mockMvc.perform(post("/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload", COURSE_SLUG, LESSON_SLUG)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fileName", "lesson.mp4",
                                "contentType", "video/mp4",
                                "sizeBytes", 512
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String objectKey = objectMapper.readTree(initResult.getResponse().getContentAsString()).get("objectKey").asText();

        mockMvc.perform(post("/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete", COURSE_SLUG, LESSON_SLUG)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("objectKey", objectKey))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Uploaded video not found for object key: " + objectKey));
    }

    @Test
    void adminCanDeleteUploadedVideo() throws Exception {
        createAdmin("admin@example.com", "Platform Admin");
        String adminToken = loginAndGetToken("admin@example.com", DEFAULT_PASSWORD);

        MvcResult initResult = mockMvc.perform(post("/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload", COURSE_SLUG, LESSON_SLUG)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fileName", "lesson.mp4",
                                "contentType", "video/mp4",
                                "sizeBytes", 512
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String objectKey = objectMapper.readTree(initResult.getResponse().getContentAsString()).get("objectKey").asText();
        inMemoryVideoStorageService.putObject(objectKey, "video/mp4", 512);

        mockMvc.perform(post("/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video-upload/complete", COURSE_SLUG, LESSON_SLUG)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("objectKey", objectKey))))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/admin/courses/{courseSlug}/lessons/{lessonSlug}/video", COURSE_SLUG, LESSON_SLUG)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isNoContent());

        Lesson lesson = lessonRepository.findByCourse_SlugAndSlug(COURSE_SLUG, LESSON_SLUG).orElseThrow();
        assertThat(lesson.getVideoUrl()).isNull();
        assertThat(lesson.getVideoStorageKey()).isNull();
    }

    private void createEnrollment(String fullName, String email, String locale) throws Exception {
        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "courseSlug", COURSE_SLUG,
                                "fullName", fullName,
                                "email", email,
                                "locale", locale
                        ))))
                .andExpect(status().isCreated());
    }

    private String registerAndGetToken(String fullName, String email, String locale) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fullName", fullName,
                                "email", email,
                                "password", DEFAULT_PASSWORD,
                                "locale", locale
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        return readAccessToken(result);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return readAccessToken(result);
    }

    private void createAdmin(String email, String fullName) {
        PlatformUser admin = new PlatformUser();
        admin.setEmail(email);
        admin.setFullName(fullName);
        admin.setLocale("en");
        admin.setRole(UserRole.ADMIN);
        admin.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        platformUserRepository.save(admin);
    }

    private String readAccessToken(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
