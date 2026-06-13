package com.fittura.global;

import com.fittura.global.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;

import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class IntegrationTestBase {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    static final MySQLContainer<?> mysql =
        new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("fittura_test")
            .withReuse(true);

    static {
        mysql.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }


    // ========== 헬퍼 메서드 ==========

    protected String userBearerToken(Long memberId) {
        return "Bearer " + jwtTokenProvider.generateAccessToken(memberId, Set.of("ROLE_USER"));
    }

    protected String adminBearerToken(Long memberId) {
        return "Bearer " + jwtTokenProvider.generateAccessToken(memberId, Set.of("ROLE_ADMIN"));
    }
}
