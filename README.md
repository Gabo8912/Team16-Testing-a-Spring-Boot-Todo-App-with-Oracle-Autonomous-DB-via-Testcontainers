````markdown
# Tutorial Rápido: Spring Boot + Oracle en Testcontainers

Este tutorial muestra cómo crear un proyecto Maven básico con Spring Boot, conectarse a una base de datos Oracle ejecutándose dentro de un contenedor Docker usando Testcontainers, y cómo escribir un test de integración simple.

---

## Requisitos

- Java 11+
- Maven 3.6+
- Docker
- Conexión a Internet para descargar imágenes de Docker y dependencias Maven

---

## 1. Crear proyecto Maven

Abre una terminal y ejecuta:

```bash
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=oracle_test_app \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
cd oracle_test_app
````

---

## 2. Añadir dependencias en `pom.xml`

Abre el archivo `pom.xml` y, dentro de la sección `<dependencies>`, añade:

```xml
<!-- Spring Boot JDBC -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>

<!-- Oracle Driver -->
<dependency>
  <groupId>com.oracle.database.jdbc</groupId>
  <artifactId>ojdbc8</artifactId>
  <version>21.7.0.0</version>
</dependency>

<!-- Testcontainers Oracle -->
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>oracle-xe</artifactId>
  <version>1.17.6</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>1.17.6</version>
  <scope>test</scope>
</dependency>
```

---

## 3. Crear script SQL para inicializar la base de datos

Crea el archivo `src/test/resources/init.sql` con el siguiente contenido:

```sql
CREATE TABLE TODOITEM (
  ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  DESCRIPTION VARCHAR2(200),
  DONE NUMBER(1)
);
```

---

## 4. Crear test de integración

Crea el archivo `src/test/java/com/example/TodoTest.java` con este contenido:

```java
package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class TodoTest {

  @Container
  static OracleContainer oracle = new OracleContainer("gvenzl/oracle-free:23.7-slim-faststart")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("init.sql");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", oracle::getJdbcUrl);
    r.add("spring.datasource.username", oracle::getUsername);
    r.add("spring.datasource.password", oracle::getPassword);
  }

  @Autowired
  JdbcTemplate jdbc;

  @Test
  void insertTwoTodos() {
    int before = jdbc.queryForObject("SELECT COUNT(*) FROM TODOITEM", Integer.class);
    jdbc.update("INSERT INTO TODOITEM (DESCRIPTION, DONE) VALUES (?, ?)", "A", 0);
    jdbc.update("INSERT INTO TODOITEM (DESCRIPTION, DONE) VALUES (?, ?)", "B", 0);
    int after = jdbc.queryForObject("SELECT COUNT(*) FROM TODOITEM", Integer.class);
    assertThat(after).isEqualTo(before + 2);
  }
}
```

---

## 5. Ejecutar tests

Para correr el test, usa:

```bash
mvn clean test
```
