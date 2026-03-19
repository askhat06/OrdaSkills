package kz.skills.elearning;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:postgresprofile;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=postgres",
        "spring.datasource.password=As-49510",
        "app.security.jwt.secret=c3VwZXItc2VjcmV0LWtleS1mb3ItcG9zdGdyZXMtdGVzdHM="
})
@ActiveProfiles("prod")
class PostgresProfileStartupTests {

    @Test
    void prodProfileLoadsWithPostgresGroupConfiguration() {
    }
}
