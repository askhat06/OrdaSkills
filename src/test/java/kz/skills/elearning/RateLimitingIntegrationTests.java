package kz.skills.elearning;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.skills.elearning.security.RequestRateLimitFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:ratelimit;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.security.jwt.secret=c3VwZXItc2VjcmV0LWtleS1mb3ItcmF0ZS1saW1pdC10ZXN0cw==",
        "app.media.video.provider=in-memory",
        "app.media.video.bucket=test-bucket",
        "app.media.video.public-base-url=https://cdn.example.test/videos",
        "app.mail.provider=in-memory",
        "app.security.rate-limit.enabled=true",
        "app.security.rate-limit.window=1m",
        "app.security.rate-limit.login-max-requests=2",
        "app.security.rate-limit.register-max-requests=2",
        "app.security.rate-limit.enrollment-max-requests=2",
        "app.security.rate-limit.resend-verification-max-requests=2"
})
@AutoConfigureMockMvc
@Transactional
class RateLimitingIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RequestRateLimitFilter requestRateLimitFilter;

    @BeforeEach
    void setUp() {
        requestRateLimitFilter.clearBuckets();
    }

    @Test
    void loginEndpointReturnsTooManyRequestsAfterLimit() throws Exception {
        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of(
                                    "email", "missing@example.com",
                                    "password", "Password123!"
                            ))))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "missing@example.com",
                                "password", "Password123!"
                        ))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Rate limit exceeded. Please try again later."));
    }

    @Test
    void registerEndpointReturnsTooManyRequestsAfterLimit() throws Exception {
        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of(
                                    "fullName", "Student " + attempt,
                                    "email", "student" + attempt + "@example.com",
                                    "password", "Password123!",
                                    "locale", "en-KZ"
                            ))))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fullName", "Student 3",
                                "email", "student3@example.com",
                                "password", "Password123!",
                                "locale", "en-KZ"
                        ))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Rate limit exceeded. Please try again later."));
    }

    @Test
    void enrollmentEndpointReturnsTooManyRequestsAfterLimit() throws Exception {
        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(post("/api/enrollments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of(
                                    "courseSlug", "digital-skills-kz",
                                    "fullName", "Lead " + attempt,
                                    "email", "lead" + attempt + "@example.com",
                                    "locale", "en-KZ"
                            ))))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "courseSlug", "digital-skills-kz",
                                "fullName", "Lead 3",
                                "email", "lead3@example.com",
                                "locale", "en-KZ"
                        ))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Rate limit exceeded. Please try again later."));
    }

    @Test
    void resendVerificationEndpointReturnsTooManyRequestsAfterLimit() throws Exception {
        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(post("/api/auth/resend-verification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(Map.of("email", "student@example.com"))))
                    .andExpect(status().isAccepted());
        }

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "student@example.com"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Rate limit exceeded. Please try again later."));
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
