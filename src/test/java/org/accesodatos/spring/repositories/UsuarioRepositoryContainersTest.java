package org.accesodatos.spring.repositories;

import jakarta.persistence.EntityManager;
import org.accesodatos.spring.models.Perfil;
import org.accesodatos.spring.models.Usuario;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@Disabled("Deshabilitado temporalmente para el despliegue en AWS")
@DataJpaTest
@Testcontainers // Habilita la gestión automática de contenedores
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"}) // Hibernate genera las tablas en el contenedor
public class UsuarioRepositoryContainersTest {

    // Configuración del contenedor PostgreSQL
    @Container
    @ServiceConnection // Conecta automáticamente las propiedades de conexión (URL, user, pass)
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17.6-alpine");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void eliminarUsuario_DebeEliminarPerfilEnCascada_RealDB() {
        // GIVEN: Datos completos para cumplir con las restricciones NOT NULL
        Usuario usuario = new Usuario();
        usuario.setUsername("testcontainers_test");
        usuario.setPassword("secret");
        usuario.setEmail("admin@testcontainers.org");

        Perfil perfil = new Perfil();
        perfil.setNombreCompleto("Testcontainers test");
        perfil.setTelefono("965111222");
        perfil.setDireccion("Avenida del Cloud 1");
        usuario.setPerfil(perfil);

        Usuario guardado = usuarioRepository.save(usuario);
        Long idPerfil = guardado.getPerfil().getId();

        // WHEN
        usuarioRepository.delete(guardado);
        usuarioRepository.flush(); // Fuerza la ejecución del DELETE en Postgres
        entityManager.clear();     // Limpia la caché para asegurar consulta a disco

        // THEN: Verificación JPA-Friendly
        assertFalse(usuarioRepository.findById(guardado.getId()).isPresent());

        // Verificamos que el perfil desapareció de la tabla real
        Perfil perfilEnBD = entityManager.find(Perfil.class, idPerfil);
        assertNull(perfilEnBD, "El perfil debería haber sido borrado por la cascada en Postgres");
    }
}
