package com.example.oracle_db_container_test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.junit.jupiter.DynamicPropertyRegistry;
import org.testcontainers.junit.jupiter.DynamicPropertySource;

@Testcontainers
@SpringBootTest
public class SpringBootDatabaseTest {
    /**
     * Use a containerized Oracle Database instance for testing.
     */
    @Container
    @ServiceConnection
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.7-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(5))
            .withUsername("Gabo89")
            .withPassword("VizcaBarca10$")
            .withInitScript("oracleTeam16.sql");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", oracleContainer::getJdbcUrl);
        registry.add("spring.datasource.username", oracleContainer::getUsername);
        registry.add("spring.datasource.password", oracleContainer::getPassword);
    }

    @Autowired
    DataSource dataSource;

    @Test
    void springDatasourceConnection() throws SQLException {
        // Query Database version to verify Spring DataSource connection
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select * from v$version");
        }
    }
}