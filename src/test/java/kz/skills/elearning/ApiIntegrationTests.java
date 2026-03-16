package kz.skills.elearning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.skills.elearning.entity.PlatformUser;
import kz.skills.elearning.entity.UserRole;
import kz.skills.elearning.repository.PlatformUserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTests {

    private static final String COURSE_SLUG = "digital-skills-kz";
    private static final String DEFAULT_PASSWORD = "Password123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatformUserRepository platformUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
