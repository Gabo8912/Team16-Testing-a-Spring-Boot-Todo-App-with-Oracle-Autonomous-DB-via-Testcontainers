package com.example.oracle_db_container_test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class TodoItemIntegrationTest {

    @Container
    static OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23.7-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(5))
            .withUsername("TODOUSER")
            .withPassword("VizcaBarca10$")
            .withInitScript("oracleTeam16.sql");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", oracleContainer::getJdbcUrl);
        registry.add("spring.datasource.username", oracleContainer::getUsername);
        registry.add("spring.datasource.password", oracleContainer::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createTwoTodoItems() {
        // Cuenta inicial de TODOITEM
        Integer countBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TODOITEM", Integer.class);

        // Inserta dos nuevas tareas (Oracle genera ID automáticamente por la columna IDENTITY)
        String insertSql = "INSERT INTO TODOITEM " +
                "(DESCRIPTION, DONE, USER_ID, SPRINT_ID, DURATION, REAL_HOURS, ESTIMATED_HOURS) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(insertSql, "Test Todo 1", 0, 6, 25, 1, 0, 1.5);
        jdbcTemplate.update(insertSql, "Test Todo 2", 0, 6, 25, 2, 0, 2.5);

        // Cuenta final de TODOITEM
        Integer countAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TODOITEM", Integer.class);

        // Verifica que se agregaron exactamente 2 filas
        assertThat(countAfter).isEqualTo(countBefore + 2);
    }
}
