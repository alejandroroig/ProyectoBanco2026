package org.accesodatos.spring;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Deshabilitado en CI/CD porque requiere PostgreSQL real")
@SpringBootTest
class ProyectoSpringBanco2026SpringApplicationTests {

    @Test
    void contextLoads() {
    }

}
