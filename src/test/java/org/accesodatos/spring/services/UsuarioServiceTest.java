package org.accesodatos.spring.services;

import org.accesodatos.spring.models.Cuenta;
import org.accesodatos.spring.models.Usuario;
import org.accesodatos.spring.repositories.UsuarioRepository;
import org.accesodatos.spring.services.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Inicializa Mockito sin cargar Spring
class UsuarioServiceTest {

    @Mock // Crea un objeto simulado UsuarioRepository vacío
    private UsuarioRepository usuarioRepository;

    @InjectMocks // Crea la instancia del servicio e inyecta los mocks dentro
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioTest;

    @BeforeEach
    void setUp() {
        usuarioTest = new Usuario();
        usuarioTest.setId(1L);
        usuarioTest.setUsername("usuario_test");
        usuarioTest.setCuentas(new ArrayList<>());
    }

    @Test
    void eliminarUsuario_Exito_SinCuentas() {
        // GIVEN (Preparación)
        Long id = 1L;
        // Simulamos que el usuario existe y su lista de cuentas está vacía
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioTest));

        // WHEN (Ejecución)
        usuarioService.eliminarUsuario(id);

        // THEN (Verificación)
        // Verificamos que se llamó al método delete del repositorio exactamente una vez
        verify(usuarioRepository, times(1)).delete(usuarioTest);
    }

    @Test
    void eliminarUsuario_Error_ConCuentas() {
        // GIVEN (Preparación)
        Long id = 1L;
        // Añadimos una cuenta al usuario para disparar la restricción lógica
        usuarioTest.getCuentas().add(new Cuenta());
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioTest));

        // WHEN & THEN (Ejecución y Verificación de la excepción)
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            usuarioService.eliminarUsuario(id);
        });

        // Verificamos que el mensaje de error sea el esperado
        assertEquals("No se puede eliminar el usuario con cuentas asociadas.", exception.getMessage());

        // Verificamos que NUNCA se llegó a llamar al delete del repositorio
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }
}