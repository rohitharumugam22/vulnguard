package com.rohith.vulnguard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=VGVzdFNlY3JldEtleUZvclRlc3RpbmdQdXJwb3Nlc09ubHlOb3RGb3JQcm9kdWN0aW9uVXNlQXRBbGwx",
        "jwt.expiration=3600000"
})
class VulnGuardApplicationTests {

    @Test
    void contextLoads() {
        // Application context loads successfully
    }
}
