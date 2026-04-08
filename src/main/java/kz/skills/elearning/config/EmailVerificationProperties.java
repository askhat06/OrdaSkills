package kz.skills.elearning.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.email-verification")
public class EmailVerificationProperties {

    private Duration tokenTtl = Duration.ofHours(24);

    private String verificationUrlBase = "http://localhost:7777/api/auth/verify-email";

    public Duration getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(Duration tokenTtl) {
        this.tokenTtl = tokenTtl;
    }

    public String getVerificationUrlBase() {
        return verificationUrlBase;
    }

    public void setVerificationUrlBase(String verificationUrlBase) {
        this.verificationUrlBase = verificationUrlBase;
    }
}
