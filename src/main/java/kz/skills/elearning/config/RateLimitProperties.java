package kz.skills.elearning.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.security.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private Duration window = Duration.ofMinutes(1);

    @Min(1)
    private int loginMaxRequests = 5;

    @Min(1)
    private int registerMaxRequests = 3;

    @Min(1)
    private int enrollmentMaxRequests = 10;

    @Min(1)
    private int resendVerificationMaxRequests = 3;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getWindow() {
        return window;
    }

    public void setWindow(Duration window) {
        this.window = window;
    }

    public int getLoginMaxRequests() {
        return loginMaxRequests;
    }

    public void setLoginMaxRequests(int loginMaxRequests) {
        this.loginMaxRequests = loginMaxRequests;
    }

    public int getRegisterMaxRequests() {
        return registerMaxRequests;
    }

    public void setRegisterMaxRequests(int registerMaxRequests) {
        this.registerMaxRequests = registerMaxRequests;
    }

    public int getEnrollmentMaxRequests() {
        return enrollmentMaxRequests;
    }

    public void setEnrollmentMaxRequests(int enrollmentMaxRequests) {
        this.enrollmentMaxRequests = enrollmentMaxRequests;
    }

    public int getResendVerificationMaxRequests() {
        return resendVerificationMaxRequests;
    }

    public void setResendVerificationMaxRequests(int resendVerificationMaxRequests) {
        this.resendVerificationMaxRequests = resendVerificationMaxRequests;
    }
}
