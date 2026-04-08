package kz.skills.elearning;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileConfigurationTests {

    @Test
    void postgresProfileFailsFastWhenJwtSecretIsMissing() {
        assertThatThrownBy(() -> {
            ConfigurableApplicationContext context = null;
            try {
                context = new SpringApplicationBuilder(ElearningBackendApplication.class)
                        .profiles("postgres")
                        .web(WebApplicationType.NONE)
                        .run(
                                "--spring.datasource.url=jdbc:h2:mem:postgresfail;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                                "--spring.datasource.driver-class-name=org.h2.Driver",
                                "--spring.datasource.username=sa",
                                "--spring.datasource.password=",
                                "--app.media.video.provider=s3",
                                "--app.media.video.bucket=test-bucket",
                                "--app.media.video.endpoint=https://storage.example.test",
                                "--app.media.video.public-base-url=https://cdn.example.test/videos",
                                "--app.media.video.access-key=test-access",
                                "--app.media.video.secret-key=test-secret",
                                "--app.media.video.region=us-east-1"
                        );
            } finally {
                if (context != null) {
                    context.close();
                }
            }
        }).hasStackTraceContaining("APP_SECURITY_JWT_SECRET");
    }
}
