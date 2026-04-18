package kz.skills.elearning;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:postgresprofile;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=postgres",
        "spring.datasource.password=",
        "app.security.jwt.secret=c3VwZXItc2VjcmV0LWtleS1mb3ItcG9zdGdyZXMtdGVzdHM=",
        "app.media.video.provider=in-memory",
        "app.media.video.bucket=test-bucket",
        "app.media.video.endpoint=https://storage.example.test",
        "app.media.video.public-base-url=https://cdn.example.test/videos",
        "app.media.video.access-key=test-access",
        "app.media.video.secret-key=test-secret",
        "app.media.video.region=us-east-1",
        "POSTGRES_URL=jdbc:h2:mem:postgresprofile;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "POSTGRES_USER=postgres",
        "POSTGRES_PASSWORD=",
        "POSTGRES_DRIVER=org.h2.Driver",
        "APP_SECURITY_JWT_SECRET=c3VwZXItc2VjcmV0LWtleS1mb3ItcG9zdGdyZXMtdGVzdHM=",
        "APP_CORS_ALLOWED_ORIGINS=http://localhost:3000",
        "APP_MEDIA_VIDEO_BUCKET=test-bucket",
        "APP_MEDIA_VIDEO_ENDPOINT=https://storage.example.test",
        "APP_MEDIA_VIDEO_PUBLIC_BASE_URL=https://cdn.example.test/videos",
        "APP_MEDIA_VIDEO_ACCESS_KEY=test-access",
        "APP_MEDIA_VIDEO_SECRET_KEY=test-secret",
        "APP_MEDIA_VIDEO_REGION=us-east-1"
})
@ActiveProfiles("prod")
class PostgresProfileStartupTests {

    @Test
    void prodProfileLoadsWithPostgresGroupConfiguration() {
    }
}
