package org.accesodatos.spring.repositories;

import jakarta.persistence.EntityManager;
import org.accesodatos.spring.models.Perfil;
import org.accesodatos.spring.models.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest // Configura una BD en memoria y carga solo entidades y repositorios
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UsuarioRepositoryIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager; // Usamos el motor de JPA para consultas directas

    @Test
    void eliminarUsuario_DebeEliminarPerfilEnCascada() {
        // 1. GIVEN: Preparamos los datos
        Usuario usuario = new Usuario();
        usuario.setUsername("usuario_test");
        usuario.setPassword("1234");
        usuario.setEmail("test@test.com");

        Perfil perfil = new Perfil();
        perfil.setNombreCompleto("Usuario Test");
        perfil.setTelefono("600111222");
        perfil.setDireccion("Calle Falsa 123");
        usuario.setPerfil(perfil); // Sincronizamos la relación bidireccional

        // Persistimos el usuario (y por cascada, el perfil)
        Usuario guardado = usuarioRepository.save(usuario);
        Long idPerfil = guardado.getPerfil().getId();

        // 2. WHEN: Ejecutamos la acción de borrado
        usuarioRepository.delete(guardado);

        /*
            IMPORTANTE: Forzamos el volcado a la BD y limpiamos la caché.
            Si no hacemos esto, entityManager.find() podría devolver el objeto
            que aún reside en la memoria (caché de primer nivel) de JPA.
        */
        usuarioRepository.flush();
        entityManager.clear();

        // 3. THEN: Verificaciones
        // Comprobamos que el usuario ya no existe usando el repositorio
        assertFalse(usuarioRepository.findById(guardado.getId()).isPresent());

        // Comprobamos que el perfil ya no existe usando el EntityManager directamentea
        // al no tener una interfaz PerfilRepository para esta comprobación
        Perfil perfilEnBD = entityManager.find(Perfil.class, idPerfil);
        assertNull(perfilEnBD, "El perfil debería haber sido borrado por la cascada de JPA");
    }
}
